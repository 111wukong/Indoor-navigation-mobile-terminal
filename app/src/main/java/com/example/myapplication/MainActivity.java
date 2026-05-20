package com.example.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE = 1;
    private static final int REQUEST_VIDEO = 2;
    private static final int REQUEST_CAMERA = 10011;

    private String mCameraImagePath;
    private Uri mCameraUri;

    static String dir = "forward";
    String filename = "";

    static {
        // OpenCV 初始化 — 必须在任何 OpenCV API 调用之前执行
        if (!OpenCVLoader.initDebug()) {
            // 如果 initDebug 失败，OpenCV Manager 会提示安装
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 图片识别
        Button btnPicture = findViewById(R.id.button0);
        btnPicture.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ChosePicture.class)));

        // 视频识别
        Button btnVideo = findViewById(R.id.button1);
        btnVideo.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ChoseVideo.class)));

        // 实时识别
        Button btnRealtime = findViewById(R.id.button3);
        btnRealtime.setOnClickListener(v ->
                startActivity(new Intent(this, ShiBie.class)));
    }

    // ------------------------------------------------------------------
    // Navigation API
    // ------------------------------------------------------------------

    public String sub(String base64Image, int type) {
        String baseUrl = "http://172.20.3.9:8086/";
        String url = baseUrl + "rest/api/navigation";

        String result = HTTPAPI.sendPost(url, "image=" + base64Image);
        try {
            JSONObject json = new JSONObject(result);
            return json.getJSONObject("data")
                    .getJSONObject("resultdata")
                    .getString("direct");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    // ------------------------------------------------------------------
    // Image processing
    // ------------------------------------------------------------------

    public void subimage(String file) {
        EditText editText = findViewById(R.id.edittext2);
        ImageView imageView = findViewById(R.id.imageView);

        Bitmap bitmap = BitmapFactory.decodeFile(file);
        if (bitmap == null) return;
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        try {
            String base64 = encodeBase64File(file);
            String direction = sub(base64, 1);
            bitmap = location3(bitmap, direction);
            editText.setText(direction);
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "识别失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ------------------------------------------------------------------
    // Video processing
    // ------------------------------------------------------------------

    public void subvideo(String file) {
        new Thread(() -> {
            EditText editText = findViewById(R.id.edittext2);
            ImageView imageView = findViewById(R.id.imageView);
            AndroidFrameConverter converter = new AndroidFrameConverter();

            try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
                grabber.start();
                int totalFrames = grabber.getLengthInFrames();

                for (int i = 1; i < totalFrames; i++) {
                    Frame frame = grabber.grabImage();
                    if (frame == null || frame.image == null) continue;
                    if (i % 2 == 0) continue; // 隔帧处理

                    Bitmap bitmap = converter.convert(frame);
                    int currentIndex = i;

                    runOnUiThread(() -> imageView.setImageBitmap(bitmap));

                    // 每 24 帧识别一次方向
                    if (currentIndex % 24 == 0 && bitmap != null) {
                        String base64 = bitmapToBase64(bitmap);
                        String direction = sub(base64, 2);
                        if (direction.length() > 2) {
                            String finalDir = direction;
                            runOnUiThread(() -> editText.setText(finalDir));
                            dir = direction;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ------------------------------------------------------------------
    // Bitmap helpers
    // ------------------------------------------------------------------

    public static Bitmap scaleMatrix(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale((float) width / w, (float) height / h);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }

    private Bitmap location3(Bitmap bmp, String dir) {
        if (dir == null || !dir.equals("forward")) return bmp;

        Bitmap watermark = BitmapFactory.decodeResource(getResources(), R.drawable.forward);
        if (watermark == null) return bmp;

        int x = bmp.getWidth();
        int y = bmp.getHeight();
        int pl = (x - watermark.getWidth()) / 2;
        int pt = y / 6;

        Bitmap result = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(watermark, pl, pt, null);
        return result;
    }

    // ------------------------------------------------------------------
    // Encoding
    // ------------------------------------------------------------------

    public static String encodeBase64File(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) return null;

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[(int) file.length()];
            int read = fis.read(buffer);
            if (read <= 0) return null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Base64.getEncoder().encodeToString(buffer)
                        .replaceAll("\r|\n", "");
            }
            return android.util.Base64.encodeToString(buffer, android.util.Base64.DEFAULT)
                    .replaceAll("\r|\n", "");
        }
    }

    private static String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Base64.getEncoder().encodeToString(bytes);
            }
            return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ------------------------------------------------------------------
    // Activity result
    // ------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;

        ImageView imageView = findViewById(R.id.imageView);

        if (requestCode == REQUEST_CAMERA && mCameraUri != null) {
            imageView.setImageURI(mCameraUri);
            return;
        }

        if (requestCode == REQUEST_IMAGE) {
            Uri uri = data.getData();
            if (uri == null) return;
            filename = getPathFromUri(uri);
            if (filename != null) {
                imageView.setImageURI(Uri.fromFile(new File(filename)));
            }
        }
    }

    @Nullable
    private String getPathFromUri(Uri uri) {
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                return getImagePath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Images.Media._ID + "=" + id
                );
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.parseLong(docId)
                );
                return getImagePath(contentUri, null);
            }
        }
        if ("content".equals(uri.getScheme())) {
            return getImagePath(uri, null);
        }
        return uri.getPath();
    }

    @Nullable
    public String getImagePath(Uri uri, String selection) {
        try (Cursor cursor = getContentResolver().query(uri, null, selection, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Permissions
    // ------------------------------------------------------------------

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0x00000012) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "相机权限被拒绝", Toast.LENGTH_LONG).show();
            }
        }
    }

    // ------------------------------------------------------------------
    // Menu
    // ------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void onLogin(MenuItem item) {
        startActivity(new Intent(this, Login.class));
    }

    public void onFeedback(MenuItem item) {
        startActivity(new Intent(this, Feedback.class));
    }

    public void onExit(MenuItem item) {
        System.exit(1);
    }
}
