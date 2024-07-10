package com.example.myapplication;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 该类为应用程序中的一个Activity，主要用于选择并处理视频文件。
 */
public class ChoseVideo extends AppCompatActivity  {

    // 视频预览ImageView组件引用
    private ImageView videoView;

    // 请求码，用于标识ACTION_PICK请求
    private static final int FILE_SELECT_CODE = 1;

    // 日志标签，用于LogCat中输出本Activity的相关信息
    private static final String TAG = "VideoActivity";

    // 临时存储处理后的帧图片
    Bitmap resbit = null;

    // 存储抽取帧线程的引用
    Thread a = null;

    /**
     * Activity的创建方法，在此方法中初始化布局和组件，并设置按钮点击事件以启动选择视频文件的Intent。
     *
     * @param savedInstanceState 应用程序保存的状态实例
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chosevideo);

        // 初始化视频预览ImageView
        videoView = (ImageView) findViewById(R.id.video_view);

        // 初始化选择视频文件按钮
        Button choice = (Button) findViewById(R.id.choicevideo);

        // 设置按钮点击事件，当点击时打开系统文件选择器选择视频文件
        choice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 创建ACTION_PICK Intent，设置类型为所有视频文件
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("video/*");
                // 启动系统文件选择器并等待结果
                startActivityForResult(intent, FILE_SELECT_CODE);
            }
        });
    }


    /**
     * 处理权限请求的结果。
     * @param requestCode 提出权限请求的唯一代码。
     * @param permissions 请求的权限数组。
     * @param grantResults 权限请求的结果数组，每个权限对应一个结果。
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 根据请求代码处理权限请求结果
        switch (requestCode) {
            case 1:
                // 检查是否对权限进行了授权
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 权限被授予，可以进行相应的操作
                } else {
                    // 权限被拒绝，提示用户并关闭当前活动
                    Toast.makeText(this, "拒绝权限将无法访问程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            // 可以处理其他请求代码的情况
            default:
        }
    }


    /**
     * 当Activity或应用即将被销毁时调用此方法，用于释放资源。
     * 该方法没有参数，也没有返回值。
     * 在此方法中，通常会进行一些必要的清理工作，比如关闭数据库、取消正在执行的任务等。
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /**
 * 获取给定URI对应的文件路径。
 * 该方法适用于多种URI类型，包括文档URI、下载文件URI、媒体文件URI以及普通的文件URI。
 * @param context 上下文对象，用于访问应用的环境信息。
 * @param uri 文件的URI，可以是文档URI、下载URI、媒体URI或其他类型的文件URI。
 * @return 根据给定的URI返回对应的文件路径。如果无法解析URI，则返回null。
 */
public static String getPath(final Context context, final Uri uri) {
    final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    // 检查是否为KitKat版本且URI为DocumentProvider类型
    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
        // 处理外部存储文档URI
        if (isExternalStorageDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];
            if ("primary".equalsIgnoreCase(type)) {
                // 对主要外部存储设备的路径进行处理
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            }
            // TODO: 处理非主要卷的逻辑
        }
        // 处理下载文件URI
        else if (isDownloadsDocument(uri)) {
            final String id = DocumentsContract.getDocumentId(uri);
            final Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            // 获取下载文件的数据列
            return getDataColumn(context, contentUri, null, null);
        }
        // 处理媒体文件URI
        else if (isMediaDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];
            // 根据文件类型（图片、视频、音频）选择对应的媒体内容URI
            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }
            // 根据URI获取媒体文件路径
            final String selection = "_id=?";
            final String[] selectionArgs = new String[]{
                    split[1]
            };
            return getDataColumn(context, contentUri, selection, selectionArgs);
        }
    }
    // 处理content类型的URI
    else if ("content".equalsIgnoreCase(uri.getScheme())) {
        return getDataColumn(context, uri, null, null);
    }
    // 处理file类型的URI，直接返回URI的路径
    else if ("file".equalsIgnoreCase(uri.getScheme())) {
        return uri.getPath();
    }
    return null; // 如果无法处理上述任何一种URI，则返回null
}



    /**
     * 从指定URI获取数据列。
     *
     * @param context 上下文对象，用于访问ContentResolver。
     * @param uri 数据的URI。
     * @param selection 查询条件，如果为null，则查询所有数据。
     * @param selectionArgs 查询条件参数，用于在selection字符串中的"?"占位符处替换值。
     * @return 返回查询到的数据，如果没有查询到数据则返回null。
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data"; // 需要查询的列
        final String[] projection = { // 查询的列集合，这里只查询_data列
                column
        };

        try {
            // 对URI进行查询，返回Cursor对象
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) { // 如果Cursor不为空且能移动到第一行
                final int column_index = cursor.getColumnIndexOrThrow(column); // 获取_data列的索引
                return cursor.getString(column_index); // 从Cursor中获取_data列的数据并返回
            }
        } finally {
            if (cursor != null) // 最后关闭Cursor
                cursor.close();
        }
        return null; // 如果没有查询到数据，则返回null
    }

    /**
     * 检查给定的Uri是否由DownloadsProvider提供。
     *
     * @param uri 需要检查的Uri。
     * @return 如果Uri的授权方是DownloadsProvider，则返回true；否则返回false。
     */
    public static boolean isDownloadsDocument(Uri uri) {
        // 比较Uri的授权方是否为"com.android.providers.downloads.documents"
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * 检查给定的Uri是否为MediaProvider。
     *
     * @param uri 需要检查的Uri。
     * @return 如果Uri的权限为MediaProvider，则返回true；否则返回false。
     */
    public static boolean isMediaDocument(Uri uri) {
        // 比较Uri的权限部分是否等于"com.android.providers.media.documents"
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * 判断给定的Uri是否表示外部存储文档。
     * <p>此方法检查Uri的授权服务器是否等于"com.android.externalstorage.documents"，
     * 这是外部存储器上文档的通用标识符。</p>
     *
     * @param uri 需要被检查的Uri对象。
     * @return 如果Uri表示外部存储文档，则返回true；否则返回false。
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        // 检查Uri的授权服务器是否为外部存储文档的标识符
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }



    /**
     * 处理从其他Activity返回的结果数据。
     *
     * @param requestCode 发起请求的代码，用于区分不同的请求。
     * @param resultCode 返回的结果代码，用于判断请求的处理结果。
     * @param data 返回的数据，包含了处理结果的详细信息。
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 当结果码为RESULT_OK时，认为请求处理成功，并尝试从中获取数据。
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData(); // 尝试从返回的Intent中获取Uri数据。

            // 检查Uri是否成功获取。
            if (uri != null) {
                // 根据Uri获取文件路径。
                String filepath = getPath(this, uri);
                // 在UI线程中执行处理视频文件的操作。
                runOnUiThread(() -> subvideo(filepath));
            }

            // 如果处理成功，则提前返回，不再继续处理。
            return;
        }

        // 当请求代码为FILE_SELECT_CODE时，额外处理逻辑。
        if (requestCode == FILE_SELECT_CODE) {
            Uri uri = data.getData(); // 获取选择的文件的Uri。
            // 打印Uri路径信息，用于调试。
            Log.i(TAG, "------->" + uri.getPath());
        }
        // 调用父类的onActivityResult方法，进行进一步的处理。
        super.onActivityResult(requestCode, resultCode, data);
    }



    /**
     * 从视频中抽帧并上传
     *
     * @param file 视频文件的路径
     * 该方法首先创建一个线程，然后在该线程中执行视频抽帧和上传的操作。它通过FFmpegFrameGrabber从视频中逐帧抓取图片，
     * 对抓取的帧进行处理（如跳过某些帧、转换为Base64编码等），然后将处理后的帧显示在ImageView中，并周期性地将帧上传。
     */
    public void subvideo(String file)
    {
        // 定义输入视频文件路径
        String videofilePath1 = file;
        // 创建并启动一个新线程来执行视频处理任务
        a = new Thread("线程名称") {
            @Override
            public void run() {
                // 清空文本视图和图像视图的内容
                EditText edittext = findViewById(R.id.edittext2);
                edittext.setText("");
                ImageView imageview = findViewById(R.id.video_view);

                // 定义输入视频文件路径
                String videofilePath = file;

                // 使用FFmpegFrameGrabber从视频文件中抓取帧
                FFmpegFrameGrabber ff = new FFmpegFrameGrabber(videofilePath);

                Thread tThread = null;
                try {
                    // 初始化FFmpegFrameGrabber
                    ff.start();
                    // 计算视频总帧数
                    int length = ff.getLengthInFrames();

                    // 遍历视频帧
                    Frame frame = null;
                    for (int i = 1; i < length; i = i + 1) {
                        frame = ff.grabImage();

                        // 跳过奇数帧
                        if (i % 2 == 1)
                            continue;

                        // 忽略空帧
                        if (frame.image == null) {
                            continue;
                        }

                        // 将帧转换为Bitmap图片
                        AndroidFrameConverter converter = new AndroidFrameConverter();
                        Bitmap frame2Bit = converter.convert(frame);

                        // 将Bitmap转换为Base64字符串并进行处理
                        String base2bit = bitmapToBase64(frame2Bit);
                        resbit = location3(frame2Bit, sub(base2bit));

                        // 在UI线程中更新图像视图
                        runOnUiThread(() -> imageview.setImageBitmap(resbit));

                        // 每24帧上传一次
                        if (i % 24 == 0) {
                            new Thread("线程名称") {
                                @Override
                                public void run() {
                                    // 更新文本视图内容为上传目录
                                    if (MainActivity.dir.length() > 2)
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
        a.start();
    }





    //添加水印
    /**
     * 在图片上添加水印。
     * @param bmp 原始图片Bitmap对象。
     * @param dir 水印方向，支持"forward", "upward", "downward", "turn_left", "turn_right"。
     * @return 添加了水印的图片Bitmap对象。
     */
    private Bitmap location3(Bitmap bmp, String dir) {
        Bitmap imgBit = null;
        Bitmap resultBmp = bmp;

        int x = bmp.getWidth(); // 获取图片宽度
        int y = bmp.getHeight(); // 获取图片高度
        BitmapFactory.Options options = new BitmapFactory.Options();

        // 根据传入的方向参数，选择不同的水印图片位置添加到原图上
        if (dir.equals("forward")) {
            // 向前方向添加水印
            BitmapFactory.decodeResource(getResources(), R.drawable.forward, options);
            int width = options.outWidth;
            int PL =220; // 计算水印左偏移量
            int PT =800; // 计算水印上偏移量
            imgBit = BitmapFactory.decodeResource(getResources(), R.drawable.forward);
            resultBmp = createWaterMaskBitmap(resultBmp, imgBit, PL, PT);
        }

        if (dir.equals("upward")) {
            // 向上方向添加水印
            BitmapFactory.decodeResource(getResources(), R.drawable.forward, options);
            int width = options.outWidth;
            int PL = 220;
            int PT = 700;
            imgBit = BitmapFactory.decodeResource(getResources(), R.drawable.forward);
            resultBmp = createWaterMaskBitmap(resultBmp, imgBit, PL, PT);
        }

        if (dir.equals("downward")) {
            // 向下方向添加水印
            BitmapFactory.decodeResource(getResources(), R.drawable.forward, options);
            int width = options.outWidth;
            int PL = 220;
            int PT = 900;
            imgBit = BitmapFactory.decodeResource(getResources(), R.drawable.forward);
            resultBmp = createWaterMaskBitmap(resultBmp, imgBit, PL, PT);
        }

        if (dir.equals("turn_left")) {
            // 向左转方向添加水印
            BitmapFactory.decodeResource(getResources(), R.drawable.left, options);
            int width = options.outWidth;
            int height = options.outHeight;
            int PL = 0 ; // 水印左偏移量
            int PT = y/2; // 水印上偏移量
            imgBit = BitmapFactory.decodeResource(getResources(), R.drawable.left);
            resultBmp = createWaterMaskBitmap(resultBmp, imgBit, PL, PT);
        }

        if (dir.equals("turn_right")) {
            // 向右转方向添加水印
            BitmapFactory.decodeResource(getResources(), R.drawable.right, options);
            int width = options.outWidth;
            int height = options.outHeight;
            int PL = 260;
            int PT = y/2;
            imgBit = BitmapFactory.decodeResource(getResources(), R.drawable.right);
            resultBmp = createWaterMaskBitmap(resultBmp, imgBit, PL, PT);
        }

        return resultBmp; // 返回添加水印后的图片
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
     * 将Bitmap图片转换为Base64编码的字符串。
     * @param bitmap 需要转换的Bitmap对象。
     * @return 转换后的Base64编码字符串，如果转换失败则返回null。
     */
    private static String bitmapToBase64(Bitmap bitmap) {
        String result = null; // 准备用于存放转换结果的字符串
        ByteArrayOutputStream baos = null; // 准备用于存放压缩后的图片数据的字节数组流
        try {
            if (bitmap != null) { // 检查传入的bitmap是否为null
                baos = new ByteArrayOutputStream(); // 创建字节数组流
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // 将bitmap压缩为JPEG格式并存入baos

                baos.flush(); // 刷新baos
                baos.close(); // 关闭baos

                byte[] bitmapBytes = baos.toByteArray(); // 将baos中的数据转为字节数组
                result = android.util.Base64.encodeToString(bitmapBytes, android.util.Base64.DEFAULT); // 使用Base64将字节数组编码为字符串
            }
        } catch (IOException e) {
            e.printStackTrace(); // 处理IO异常
        } finally {
            try {
                if (baos != null) { // 确保baos被正确关闭
                    baos.flush(); // 刷新baos
                    baos.close(); // 关闭baos
                }
            } catch (IOException e) {
                e.printStackTrace(); // 处理关闭baos时的IO异常
            }
        }
        return result; // 返回转换后的Base64字符串
    }



    /**
     * 通过上传图片参数s，向指定URL发送POST请求，获取导航方向信息，并将该信息显示在EditText中。
     * @param s 图片的字符串表示，用于上传
     * @return 返回解析出的导航方向字符串，若解析失败则返回空字符串
     */
    public String sub(String s) {
        EditText edittext = findViewById(R.id.edittext2); // 获取EditText对象，用于显示导航方向
        String baseurl = "http://172.20.3.9:8086/"; // 定义基础URL
        String url = baseurl + "rest/api/navigation"; // 定义请求的完整URL
        String param = "image=" + s; // 构造请求参数
        // 发送POST请求，获取响应结果
        String result = HTTPAPI.sendPost(url, param);
        try {
            JSONObject jsonObject = null;
            jsonObject = new JSONObject(result); // 尝试解析响应结果为JSONObject
            // 从响应结果中提取导航方向信息
            String direct = jsonObject.getJSONObject("data").getJSONObject("resultdata").getString("direct");
            edittext.setText(direct); // 将导航方向信息显示在EditText中
            return direct; // 返回导航方向信息
        } catch (JSONException e) {
            e.printStackTrace(); // 解析异常处理
        }
        return ""; // 解析失败，返回空字符串
    }


    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree 旋转的角度。返回值为图片需要旋转的角度，如90度、180度或270度；如果图片不需要旋转，则返回0度。
     */
    public int readPictureDegree(String path) {
        int degree = 0; // 初始化旋转角度为0
        try {
            // 通过ExifInterface读取图片的Exif信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转方向
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            // 根据旋转方向确定图片需要旋转的角度
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            // 打印IOException异常堆栈跟踪信息
            e.printStackTrace();
        }
        return degree; // 返回旋转角度
    }



    /*
     * 旋转图片
     * @param angle 旋转角度
     * @param bitmap 需要旋转的图片
     * @return Bitmap 旋转后的图片
     */
    public Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        // 创建一个矩阵，用于图片的旋转
        Matrix matrix = new Matrix();
        // 在矩阵上执行旋转操作
        matrix.postRotate(angle);
        // 根据旋转后的矩阵重新创建一张图片
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }



}