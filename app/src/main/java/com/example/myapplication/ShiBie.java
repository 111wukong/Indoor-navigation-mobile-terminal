package com.example.myapplication;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Base64;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class ShiBie extends CameraActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    /**
     * CV相机
     */
    private CameraBridgeViewBase mCVCamera;
    /**
     * 加载OpenCV的回调
     */
    private BaseLoaderCallback mLoaderCallback;
    private ImageView image_view;




    /**
     * 创建Activity时调用的函数。
     * @param savedInstanceState 如果Activity被系统重新创建，这个参数包含了之前Activity结束时的状态。否则是null。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // 调用父类的onCreate函数，初始化Activity
        setContentView(R.layout.shibie); // 设置Activity的布局文件
        initView(); // 初始化视图及组件
    }

    /**
     * 初始化视图组件和OpenCV相机。
     * 本函数负责初始化UI组件，如图像视图和相机视图，并设置相机视图的相关监听器。
     * 同时，通过连接到OpenCV的回调以在成功加载OpenCV库时启用相机视图。
     */
    private void initView()  {
        image_view = findViewById(R.id.image_view);  // 初始化图像视图
        // 初始化CV相机视图并设置其可见性
        mCVCamera = findViewById(R.id.camera_view);
        mCVCamera.setVisibility(CameraBridgeViewBase.VISIBLE);
        // 设置相机视图的监听器，用于处理相机相关的事件
        mCVCamera.setCvCameraViewListener(this);
        // 创建并设置OpenCV加载器回调，以处理OpenCV库加载成功或失败的逻辑
        mLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                // 根据库加载的状态执行相应的操作
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        // 当库加载成功时，启用相机视图
                        mCVCamera.enableView();
                        break;
                    default:
                        // 处理加载失败的情况，此处代码省略
                        break;
                }
            }
        };
    }


    /**
     * 初始化Debug模式下的OpenCV库。
     * 该函数尝试初始化OpenCV库，并在成功初始化时调用mLoaderCallback的onManagerConnected方法。
     * 参数：无
     * 返回值：无
     */
    private void initDabug() {
        // 尝试初始化OpenCV库，如果成功则调用回调函数
        if (OpenCVLoader.initDebug()) {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    /**
     * 获取摄像头视图列表。
     * 该方法被用于获取当前实例中定义的摄像头视图。在本实现中，它返回了一个包含单个摄像头视图元素的列表。
     *
     * @return List<? extends CameraBridgeViewBase> 返回一个包含摄像头视图的列表。列表中的元素是CameraBridgeViewBase的子类。
     */

    @Override
    public List<? extends CameraBridgeViewBase> getCameraViewList() {
        // 返回包含单个摄像头视图mCVCamera的列表
        return Arrays.asList(mCVCamera);
    }

    /**
     * 当摄像头视图开始时被调用。
     * 这个方法是在摄像头成功打开并且视图准备就绪后被调用的，可以用于初始化与摄像头视图相关的操作。
     * @param width  视图的宽度。
     * @param height 视图的高度。
     */
    @Override
    public void onCameraViewStarted(int width, int height) {
        // 此处可以添加与摄像头视图大小相关的初始化代码
    }

    /**
     * 当摄像头视图停止时调用此方法。
     * 该方法是一个回调方法，当摄像头视图因为某些原因（如应用切换到后台、摄像头关闭等）停止时被调用。
     * 这个方法不接受任何参数，也不返回任何值。
     */
    @Override
    public void onCameraViewStopped() {
        // 在这里可以进行摄像头视图停止后的相关处理，例如释放资源等
    }

    /**
     * 对相机帧进行处理的函数。
     *
     * @param inputFrame 输入的相机帧，包含灰度图或彩色图。
     * @return 处理后的图像Mat对象。注意，本函数实现中返回了null，意味着不返回处理后的图像。
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // 获取相机传入的图像，并将其旋转90度
        Mat rgba = inputFrame.rgba();
        Core.rotate(rgba, rgba, Core.ROTATE_90_CLOCKWISE);

        // 将Mat图像转换为Bitmap格式
        Bitmap bitmap = Bitmap.createBitmap(rgba.cols(), rgba.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgba, bitmap);

        Bitmap location = location(bitmap);

        // 如果location不为null，说明图像处理成功，接下来进行Base64编码和显示
        if (location != null) {
            Bitmap finalLocation = location;
            // 将Bitmap图像转换为Base64字符串
            String s = bitmapToBase64(finalLocation);
            // 对Base64字符串进行子串提取或处理
            String dir=sub(s);
            // 在UI线程中更新图像视图
            runOnUiThread(() -> image_view.setImageBitmap(location2(bitmap,dir)));
        }
        return null;
    }

    /**
     * 从服务器获取导航方向的指令。
     * @param s 作为图像文件名的字符串参数，用于上传至服务器。
     * @return 返回服务器返回的导航方向字符串。如果处理失败，则返回空字符串。
     */
    public String sub(String s) {
        // 查找EditText视图，用于后续显示导航方向
        EditText editText = findViewById(R.id.edittext2);
        // 定义基础URL
        String baseUrl = "http://172.20.3.9:8086/";
        // 拼接完整URL，用于导航API的访问
        String url = baseUrl + "rest/api/navigation";
        // 构造请求参数，包含上传的图像文件名
        String param = "image=" + s;
        // 发送POST请求，并获取响应结果
        String result = HTTPAPI.sendPost(url, param);
        try {
            // 解析JSON响应结果
            JSONObject jsonObject = null;
            jsonObject = new JSONObject(result);
            // 从JSON对象中提取导航方向信息，并将其显示在EditText中
            String direct = jsonObject.getJSONObject("data").getJSONObject("resultdata").getString("direct");
            editText.setText(direct);
            return direct;
        } catch (JSONException e) {
            // 处理JSON解析异常
            e.printStackTrace();
        }
        // 如果出现异常，返回空字符串
        return "";
    }


    /**
     * 对给定的Bitmap图像进行高斯模糊和边缘检测处理。
     *
     * @param bmp 输入的Bitmap图像。
     * @return 处理后的Bitmap图像，包含了高斯模糊和Canny边缘检测的结果。
     */
    private Bitmap location(Bitmap bmp) {
        // 将Bitmap图像转换为OpenCV的Mat格式
        Mat originMat=new Mat();
        Utils.bitmapToMat(bmp,originMat);

        // 初始化用于中间处理和最终结果的Mat对象
        Mat resultG = new Mat();
        Mat result = new Mat();

        // 对原始图像进行高斯模糊处理，以减少噪声
        Imgproc.GaussianBlur(originMat, resultG, new Size(3.0, 3.0), 0);

        // 使用Canny算法进行边缘检测
        Imgproc.Canny(resultG, result, 100.0, 220.0, 3);

        // 将处理后的Mat图像转换回Bitmap格式
        Bitmap resultBmp = Bitmap.createBitmap(resultG.cols(), resultG.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(resultG,resultBmp);

        // 返回处理后的Bitmap图像
        return resultBmp;
    }




    /**
     * 在图像上添加水印。
     * @param bmp 原始Bitmap图像。
     * @param dir 水印方向，根据不同的方向添加不同的水印图像。
     * @return 添加了水印的Bitmap图像。
     */
    private Bitmap location2(Bitmap bmp,String dir) {
        // 将原始Bitmap转换为Mat格式
        Mat originMat = new Mat();
        Utils.bitmapToMat(bmp, originMat);

        // 初始化处理后的Mat图像及最终结果Mat
        Mat resultG = new Mat();
        Mat result = new Mat();

        // 创建最终的Bitmap图像，用于显示结果
        Bitmap imgBit = null;
        Imgproc.GaussianBlur(originMat, resultG, new Size(3.0, 3.0), 0);
        Imgproc.Canny(resultG, result, 100.0, 220.0, 3);

        Bitmap resultBmp = Bitmap.createBitmap(resultG.cols(), resultG.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(resultG, resultBmp);

        // 根据传入的方向参数，选择不同的水印图片并添加到结果图像上
        if(dir.equals("forward")) {
            imgBit = BitmapFactory.decodeResource(getResources(), R.drawable.forward);
            resultBmp = createWaterMaskBitmap(resultBmp, imgBit, 20, 100);
        }
        if(dir.equals("upward")) {
            imgBit = BitmapFactory.decodeResource(getResources(), R.drawable.forward);
            resultBmp = createWaterMaskBitmap(resultBmp, imgBit, 20, 100);
        }

        if(dir.equals("downward")) {
            imgBit = BitmapFactory.decodeResource(getResources(), R.drawable.forward);
            resultBmp = createWaterMaskBitmap(resultBmp, imgBit, 20, 100);
        }

        if(dir.equals("turn_left")) {
            imgBit = BitmapFactory.decodeResource(getResources(), R.drawable.left);
            resultBmp = createWaterMaskBitmap(resultBmp, imgBit, 20, 100);
        }

        if(dir.equals("turn_right")) {
            imgBit = BitmapFactory.decodeResource(getResources(), R.drawable.right);
            resultBmp = createWaterMaskBitmap(resultBmp, imgBit, 20, 100);
        }

        // 返回添加了水印的Bitmap图像
        return resultBmp;
    }



    /**
     * 创建带有水印的图片。
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
     * 将bitmap图像转换成file类型并保存到指定路径。
     * @param bm 需要保存的Bitmap图像。
     * @param path 保存图片的文件路径。
     * @param fileName 保存后的图片文件名。
     * @return 返回保存图片的File对象。
     * @throws IOException 如果在写文件过程中发生IO异常。
     */
    public static File saveFile(Bitmap bm,String path, String fileName) throws IOException {
        // 创建或检查目标文件夹是否存在
        File dirFile = new File(path);
        if(!dirFile.exists()){
            dirFile.mkdir();
        }
        // 创建文件对象，用于保存图片
        File myCameraFile = new File(path , fileName);
        // 使用BufferedOutputStream向文件中写入Bitmap数据
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCameraFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos); // 压缩图片并写入文件
        bos.flush(); // 刷新输出流
        bos.close(); // 关闭输出流
        return myCameraFile; // 返回保存的文件对象
    }


    /**
     * 将Bitmap图片转换为Base64编码的字符串。
     *
     * @param bitmap 需要转换的Bitmap对象。
     * @return 转换后的Base64编码字符串，如果转换失败则返回null。
     */
    private static String bitmapToBase64(Bitmap bitmap) {
        String result = null; // 准备用于存放转换结果的字符串
        ByteArrayOutputStream baos = null; // 准备用于存放压缩后的图片数据的字节数组流
        try {
            if (bitmap != null) { // 检查传入的bitmap是否为null
                baos = new ByteArrayOutputStream(); // 创建字节数组流实例
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // 将bitmap压缩为JPEG格式并存储到baos中

                baos.flush(); // 清空字节数组流
                baos.close(); // 关闭字节数组流

                byte[] bitmapBytes = baos.toByteArray(); // 将字节数组流转换为字节数组
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT); // 将字节数组转换为Base64编码的字符串
            }
        } catch (IOException e) {
            e.printStackTrace(); // 处理IO异常
        } finally {
            try {
                if (baos != null) { // 确保baos不为null
                    baos.flush(); // 清空字节数组流
                    baos.close(); // 关闭字节数组流
                }
            } catch (IOException e) {
                e.printStackTrace(); // 处理关闭流时可能出现的IO异常
            }
        }
        return result; // 返回转换后的Base64编码字符串
    }



    /**
     * 当界面重新恢复时调用的方法。
     * 该方法会初始化调试环境并通知OpenCV连接成功。
     * 无参数。
     * 无返回值。
     */
    @Override
    protected void onResume() {
        // 初始化调试环境并发送连接成功的信号
        initDabug();
        super.onResume(); // 调用父类的onResume方法
    }

    /**
     * 当Activity暂停时调用此方法。重写onPause()以确保在暂停时正确处理资源。
     * 主要进行OpenCV相机的销毁操作，以释放资源。
     */
    @Override
    protected void onPause() {
        super.onPause(); // 调用父类的onPause方法
        // 如果mCVCamera不为空，则销毁OpenCV相机视图
        if (mCVCamera != null)
            mCVCamera.disableView(); // 销毁相机视图，释放资源
    }

}

