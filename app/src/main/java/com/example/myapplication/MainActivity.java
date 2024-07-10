package com.example.myapplication;

import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_TAKE_PICTURE = 1000;
    // 申请相机权限的requestCode
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 0x00000012;

    private static final int CAMERA_REQUEST_CODE = 10011;



    //用于保存拍照图片的uri
    private Uri mCameraUri;

    // 用于保存图片的文件路径，Android 10以下使用图片路径访问图片
    private String mCameraImagePath;
    Thread a=null;
    String filename = "";
    static  String dir="forward";

/**
 * 当活动被创建时调用。
 * 主要负责初始化界面和设置点击事件。
 *
 * @param savedInstanceState 如果活动之前被销毁，这参数包含之前的状态。如果活动没被销毁之前，这参数是null。
 */
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    // 设置严格模式，检测磁盘读写和网络访问，违规则记录日志
    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
    setContentView(R.layout.activity_main); // 设置使用的布局文件
    // 初始化工具栏并设置为活动的工具栏
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    // 初始化并设置选择视频按钮的点击事件
    Button button1 = findViewById(R.id.button1);
    Intent intentsp =new Intent(MainActivity.this,ChoseVideo.class);
    button1.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 启动选择视频的活动
            startActivity(intentsp);
        }
    });
    // 初始化并设置实时识别按钮的点击事件
    Button button3 = findViewById(R.id.button3);
    Intent intentsb =new Intent(this,ShiBie.class);
    button3.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 启动实时识别的活动
            startActivity(intentsb);
        }
    });
    // 初始化并设置图片识别按钮的点击事件
    Button button0 = findViewById(R.id.button0);
    Intent intentp = new Intent(MainActivity.this, ChosePicture.class);
    button0.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(intentp);
        }
    });

}

    /**
     * 图片识别并返回方向信息。
     * @param s 图片的Base64编码字符串
     * @param type 识别类型，目前未使用，预留参数
     * @return 图片识别结果，返回的是一个表示方向的字符串
     */
    public String sub(String s, int type) {

        String baseurl = "http://172.20.3.9:8086/";
        // 默认导航API URL
        String url = baseurl + "rest/api/navigation";
        // 当type为2时，也使用默认导航API URL
        if (type == 2)
            url = baseurl + "rest/api/navigation";
        // 构造请求参数，将图片Base64编码作为参数传递
        String param = "image=" + s;

        // 发送POST请求并获取响应结果
        String result = HTTPAPI.sendPost(url, param);
        try {
            // 解析JSON响应结果
            JSONObject jsonObject = null;
            jsonObject = new JSONObject(result);
            // 从响应中提取方向信息
            String direct = jsonObject.getJSONObject("data").getJSONObject("resultdata").getString("direct");
            return direct;
        } catch (JSONException e) {
            // 处理JSON解析异常
            e.printStackTrace();
        }
        // 如果出现异常等情况，返回空字符串
        return "";
    }



/**
 * 从指定文件创建图片的子图像，并显示在文本框和图像视图中。
 * @param file 要处理的图片文件的路径。
 */
public void subimage(String file) {
    EditText edittext = findViewById(R.id.edittext2);  // 查找编辑文本视图
    ImageView image_view = findViewById(R.id.imageView); // 查找图像视图
    String filePath = file;

    // 通过BitmapFactory.decodeFile()方法加载图片，并创建一个可修改的Bitmap副本
    Bitmap bitmap = BitmapFactory.decodeFile(filePath).copy(Bitmap.Config.ARGB_8888, true);

    try {
        String s = encodeBase64File(filePath);  // 将文件编码为Base64字符串
        String direct = sub(s, 1);  // 对Base64字符串进行处理（具体操作不详）

        // 对图像进行处理（推测是根据'direct'的指令裁剪或修改图像）
        bitmap = location3(bitmap, direct);

        // 显示处理结果：将文本设置到编辑文本视图，将图像设置到图像视图
        edittext.setText(direct);
        image_view.setImageBitmap(bitmap);
    } catch (Exception e) {
        e.printStackTrace();
    }
}






    /**
     * 从视频中提取帧并进行处理的函数
     * @param file 视频文件的路径
     */
    public void subvideo(String file)
    {
        // 初始化变量，设置视频文件路径
        String videofilePath1 = file;
        // 创建并启动一个新线程来处理视频帧
        a=new Thread("线程名称") {
            // 复写run方法，定义线程的具体行为
            @Override
            public void run() {
                // 初始化UI组件
                EditText edittext = findViewById(R.id.edittext2);
                edittext.setText("");
                ImageView imageview = findViewById(R.id.imageView);
                // 设置视频文件路径
                String videofilePath = file;

                // 使用FFmpegFrameGrabber来抓取视频帧
                FFmpegFrameGrabber ff = new FFmpegFrameGrabber(videofilePath);

                // 初始化线程变量
                Thread tThread=null;
                try {
                    // 启动FFmpegFrameGrabber
                    ff.start();
                    // 计算视频总帧数
                    int length = ff.getLengthInFrames();
                    // 遍历视频帧
                    Frame frame = null;
                    for (int i = 1; i < length; i=i+1) {
                        // 抓取视频帧
                        frame = ff.grabImage();
                        // 跳过偶数帧
                        if (i % 2 ==1)
                            continue;
                        // 检查帧是否为空
                        if (frame.image == null) {
                            continue;
                        }
                        // 将帧转换为Bitmap图片
                        AndroidFrameConverter converter = new AndroidFrameConverter();
                        Bitmap frame2Bit = converter.convert(frame);
                        // 在UI线程中更新图片视图
                        runOnUiThread(() -> imageview.setImageBitmap(frame2Bit));
                        // 将图片转换为Base64字符串
                        String base2bit = bitmapToBase64(frame2Bit);
                        // 每24帧执行一次上传操作
                        if (i % 24 == 0) {
                            // 在新线程中处理上传逻辑
                            new Thread("线程名称") {
                                @Override
                                public void run() {
                                    // 处理并更新UI
                                    MainActivity.dir = sub(base2bit,2);
                                    if(MainActivity.dir.length()>2)
                                        edittext.setText(MainActivity.dir);
                                }
                            }.start();
                        }
                    }
                    // 停止FFmpegFrameGrabber
                    ff.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        // 启动线程
        a.start();
    }






    /**
     * 使用Matrix 调整比例，缩放原始的Bitmap以适应给定的宽度和高度。
     * @param bitmap 原始的Bitmap图像。
     * @param width 目标宽度，缩放后的图像宽度将以此为准。
     * @param height 目标高度，缩放后的图像高度将以此为准。
     * @return 缩放后的Bitmap图像。
     */
    public static Bitmap scaleMatrix(Bitmap bitmap, int width, int height){
        // 获取原始Bitmap的宽度和高度
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        // 计算宽度和高度的缩放比例
        float scaleW = width/w;
        float scaleH = height/h;

        // 创建Matrix并设置缩放比例
        Matrix matrix = new Matrix();
        matrix.postScale(scaleW, scaleH); // 设置对Bitmap进行缩放的Matrix

        // 使用缩放后的Matrix生成新的Bitmap并返回
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }




    /**
     * 给图片添加水印。
     * @param bmp 原始图片Bitmap对象。
     * @param dir 水印方向，包括"forward", "upward", "downward", "turn_left", "turn_right"。
     * @return 添加水印后的图片Bitmap对象。
     */
    private Bitmap location3(Bitmap bmp,String dir) {
        Bitmap imgBit = null;
        Bitmap resultBmp = bmp;

        int x =bmp.getWidth(); // 获取图片宽度
        int y =bmp.getHeight(); // 获取图片高度
        BitmapFactory.Options options = new BitmapFactory.Options();

        // 根据不同的方向添加不同的水印
        if(dir.equals("forward")) {
            // 解析水印图片并计算位置
            BitmapFactory.decodeResource(getResources(),R.drawable.forward,options);
            int width = options.outWidth;
            int height = options.outHeight;
            int PL = (x-width)/2; // 水印左上角相对于原图的水平位置
            int PT = y/6; // 水印左上角相对于原图的垂直位置
            imgBit = BitmapFactory.decodeResource(getResources(), R.drawable.forward);
            resultBmp = createWaterMaskBitmap(resultBmp, imgBit, PL, PT);
        }
        return resultBmp;
    }


    /**
     * 创建带有水印的图片。
     *
     * @param src 原始图片。
     * @param watermark 水印图片。
     * @param paddingLeft 水印图片左边距。
     * @param paddingTop 水印图片上边距。
     * @return 带有水印的新图片。
     */
    private static Bitmap createWaterMaskBitmap(Bitmap src, Bitmap watermark, int paddingLeft, int paddingTop) {
        if (src == null) {
            return null;
        }
        int width = src.getWidth();
        int height = src.getHeight();

        // 创建一个新的和SRC长度宽度一样的位图
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // 将原始图片绘制到新位图上
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(src, 0, 0, null);

        // 在指定位置绘制水印图片
        canvas.drawBitmap(watermark, paddingLeft, paddingTop, null);

        return newBitmap;
    }





    /**
     * 处理权限申请的回调。
     * 当用户对应用的权限请求做出响应时，系统会调用此方法。
     *
     * @param requestCode  请求码，用于标识发起权限请求的特定动作。
     * @param permissions  用户授权的权限数组。
     * @param grantResults  授权结果数组，每个元素对应权限请求的结果。
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 当请求码匹配 PERMISSION_CAMERA_REQUEST_CODE 时处理相机权限的请求结果
        if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            // 检查权限是否被授权
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授权，可以执行需要相机权限的操作，例如打开相机
                //   openCamera();
            } else {
                // 用户拒绝授权，可以给用户一个提示
                Toast.makeText(this,"拍照权限被拒绝",Toast.LENGTH_LONG).show();
            }
        }
    }




    /**
     * 当从外部活动返回时调用此方法，例如使用相机或图库应用选择图片。
     * @param requestCode 发起请求的代码，用于区分不同的请求。
     * @param resultCode 返回的结果代码，用于表示操作成功或失败。
     * @param data 返回的数据，包含选择的文件或图片的URI。
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 查找编辑文本和图片视图
        EditText edittext = findViewById(R.id.edittext2);
        ImageView imageview = findViewById(R.id.imageView);

        // 如果请求代码是拍摄照片的代码，将拍摄的照片设置到图片视图中
        if (requestCode == CAMERA_REQUEST_CODE) {
            imageview.setImageURI(mCameraUri);
        }

        // 根据请求代码处理不同的逻辑
        switch (requestCode) {
            case 1:
                // 处理Android 4.4以上版本选择图片的逻辑
                if (Build.VERSION.SDK_INT >= 19) {
                    Uri uri = data.getData();
                    if (DocumentsContract.isDocumentUri(this, uri)) {
                        // 解析文档URI
                        String docId = DocumentsContract.getDocumentId(uri);
                        if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                            String id = docId.split(":")[1];
                            String selection = MediaStore.Images.Media._ID + "=" + id;
                            filename = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                        } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                            Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                            filename = getImagePath(contentUri, null);
                        }
                    } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                        filename = getImagePath(uri, null);
                    }

                    // 获取文件类型
                    String fileTyle=filename.substring(filename.lastIndexOf("."),filename.length());

                    // 根据文件类型处理图片或视频
                    if(fileTyle.equals(".mp4")) {
                        // 处理视频文件，将其帧转换为图片显示在ImageView上
                        FFmpegFrameGrabber ff = new FFmpegFrameGrabber(filename);
                        try {
                            ff.start();
                            Frame frame = null;
                            frame = ff.grabImage();
                            if (frame.image == null) {
                                break;
                            }
                            AndroidFrameConverter converter = new AndroidFrameConverter();
                            Bitmap frame2Bit = converter.convert(frame);
                            imageview.setImageBitmap(frame2Bit);
                            ff.stop();
                        } catch (FrameGrabber.Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    else
                    {
                        // 处理其他类型的图片文件
                        imageview.setImageURI(Uri.fromFile(new File(filename)));
                    }
                }
                break;
        }
    }




    /**
     * 根据Uri和选择条件获取图片路径。
     * @param uri 图片的Uri
     * @param selection 查询条件
     * @return 图片的文件路径，如果找不到则返回null。
     */
    public String getImagePath(Uri uri, String selection) {
        String path = null;
        // 使用Uri和选择条件查询图片信息
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            // 如果查询结果不为空，尝试获取第一行数据中的图片路径
            if (cursor.moveToFirst()) {
                path = cursor.getString(Math.abs(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
            }
            // 关闭Cursor
            cursor.close();
        }

        return path;
    }






    /**
     * 将指定文件转换为base64编码字符串。
     *
     * @param filename 需要转换的文件的路径。
     * @return 转换后的base64编码字符串。如果文件不存在，则返回null。
     * @throws Exception 如果读取文件或进行base64编码过程中发生错误。
     */
    public static String encodeBase64File(String filename) throws Exception {
        File file = new File(filename);
        if (!file.exists()) { // 检查文件是否存在
            return null;
        }
        // 尝试读取文件内容并进行base64编码
        String context = null;
        try {
            FileInputStream inputFile = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()]; // 根据文件长度创建对应长度的字节数组
            inputFile.read(buffer); // 读取文件内容到字节数组
            inputFile.close(); // 关闭文件输入流
            // 对字节数组进行base64编码，并移除编码后的换行符
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context = Base64.getEncoder().encodeToString(buffer).replaceAll("\r|\n", "");
            }
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常信息
        }
        return context;
    }



    /**
     * 识别图片
     */

    /**
     * 将Bitmap图片转换为Base64编码的字符串。
     *
     * @param bitmap 需要转换的Bitmap对象。
     * @return 转换后的Base64编码字符串，如果转换失败则返回null。
     */
    private static String bitmapToBase64(Bitmap bitmap) {
        String result = null; // 初始化结果为null
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) { // 检查bitmap是否为null
                baos = new ByteArrayOutputStream(); // 创建ByteArrayOutputStream对象用于存储压缩后的图片数据

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // 将bitmap压缩为JPEG格式并存储到baos中

                baos.flush(); // 清空baos中的数据
                baos.close(); // 关闭baos

                byte[] bitmapBytes = baos.toByteArray(); // 将baos中的数据转换为字节数组
                result = android.util.Base64.encodeToString(bitmapBytes, android.util.Base64.DEFAULT); // 将字节数组转换为Base64编码的字符串
            }
        } catch (IOException e) {
            e.printStackTrace(); // 处理IOException异常
        } finally {
            try {
                if (baos != null) { // 确保baos被正确关闭
                    baos.flush(); // 清空baos
                    baos.close(); // 关闭baos
                }
            } catch (IOException e) {
                e.printStackTrace(); // 处理关闭baos时可能发生的IOException异常
            }
        }
        return result; // 返回转换后的Base64编码字符串
    }



    /**
     * 创建选项菜单。
     * 这个方法用于为应用程序的主菜单创建选项菜单。当系统准备显示菜单时调用此方法。
     *
     * @param menu 用于向菜单中添加项目的Menu对象。
     * @return 总是返回true，允许系统调用onCreateOptionsMenu()来显示菜单。
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 将菜单资源文件中的项目添加到menu对象中。如果action bar存在，这些项目将显示在action bar中。
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * 处理选项菜单项的选择事件。
     * <p>此方法会根据菜单项的ID来处理不同的点击事件。在AndroidManifest.xml中指定了父活动的情况下，
     * 动作栏会自动处理Home/Up按钮的点击事件。</p>
     *
     * @param item 选中的菜单项
     * @return 如果事件已成功处理，则返回true；否则如果事件未处理，则返回false。
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 获取点击的菜单项的ID
        int id = item.getItemId();
        // 默认情况下，将事件传递给父类处理
        return super.onOptionsItemSelected(item);
    }
    public void Login(MenuItem item) {
        Intent intent1 = new Intent(this, Login.class);
        startActivity(intent1);
    }
    public void Share(MenuItem item) {
    }
    public void Feedback(MenuItem item) {
        Intent intent2 = new Intent(this, Feedback.class);
        startActivity(intent2);
    }

    public void Exit(MenuItem item) {
        System.exit(1);
    }



}







