package com.example.myapplication;

import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

public class HTTPAPI {


    public static String uploadFile(String url, Map<String, String> params, File file) {
        // 换行，或者说是回车
        //final String newLine = "\r\n";
        final String newLine = "\r\n";
// 固定的前缀
        final String preFix = "--";
        //final String preFix = "";
// 分界线，就是上面提到的boundary，可以是任意字符串，建议写长一点，这里简单的写了一个#
        final String bounDary = "----WebKitFormBoundaryCXRtmcVNK0H70msG";
//final String bounDary = "";
        //请求返回内容
        String output = "";
        HttpURLConnection httpURLConnection;

        System.out.println("nnn:init:");
        try {
            //统一资源定位符
            URL uploadFileUrl = new URL(url);
            //打开http链接类
            httpURLConnection = (HttpURLConnection) uploadFileUrl.openConnection();
            //设置是否向httpURLConnection输出
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            //设置请求方法默认为get
            httpURLConnection.setRequestMethod("POST");
            //Post请求不能使用缓存
       //     httpURLConnection.setUseCaches(false);
            //设置token
            // httpURLConnection.setRequestProperty("authorization", (String) GlobalContext.getSessionAttribute("token"));
            //为web端请求
        //    httpURLConnection.setRequestProperty("os", "web");
            //从新设置请求内容类型
          //  httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
          //  httpURLConnection.setRequestProperty("Accept", "*/*");
           // httpURLConnection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        //    httpURLConnection.setRequestProperty("Cache-Control", "no-cache");
            //application/json;charset=UTF-8 application/x-www-form-urlencoded
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;charset=UTF-8");
          //  httpURLConnection.setRequestProperty("User-Agent", "(Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.84 Safari/537.36)");
            httpURLConnection.setRequestProperty("connection", "Keep-Alive");
            System.out.println("nnn:init0:");
            httpURLConnection.connect();
            System.out.println("nnn:init1:");
            httpURLConnection.getOutputStream();
            System.out.println("nnn:init2:");
            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
            System.out.println("nnn:init3:");
            //设置DataOutputStream设置DataOutputStream数据输出流
            OutputStream outputStream = httpURLConnection.getOutputStream();

//上传普通文本文件
            System.out.println("nnn:init:");
            if (params.size() != 0 && params != null) {
                System.out.println("nnn:up text:");
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    //获取参数名称和值
                    String key = entry.getKey();
                    String value = params.get(key);
//向请求中写分割线
                    dos.writeBytes(preFix + bounDary + newLine);
                    //向请求拼接参数
                    //String parm = key + "=" + URLEncoder.encode(value,"utf-8") +"\r\n" ;
                    dos.writeBytes("Content-Disposition: form-data; " + "name=\"" + key + "\"" + newLine);
                    //向请求中拼接空格
                    dos.writeBytes(newLine);
                    //写入值
                    dos.writeBytes(value);
                    //dos.writeBytes(value);
                    //向请求中拼接空格
                    dos.writeBytes(newLine);
                }
            }

            //上传文件
            if (file != null && !params.isEmpty()) {
                //向请求中写分割线
                //把file装换成byte
                File del = new File(file.toURI());
                InputStream inputStream = new FileInputStream(del);


                byte[] bytes = input2byte(inputStream);
                String filePrams = "file";
                String fileName = file.getName();
                //向请求中加入分隔符号
             //   dos.write((preFix + bounDary + newLine).getBytes());
                //将byte写入
              //  dos.writeBytes("Content-Disposition: form-data; " + "name=\"" + URLEncoder.encode(filePrams, "utf-8") + "\"" + "; filename=\"" + URLEncoder.encode(fileName, "utf-8") + "\"" + newLine);
               // dos.writeBytes(newLine);
                dos.write(bytes);
                //向请求中拼接空格
             //   dos.writeBytes(newLine);
            }
            dos.writeBytes(preFix + bounDary + preFix + newLine);

            System.out.println("nnn:write:");
            //请求完成后关闭流
            //得到相应码
            dos.flush();
            //判断请求没有成功
            if (httpURLConnection.getResponseCode() != 200) {
                //logger.error(url + "   请求异常，错误代码为：  " + httpURLConnection.getResponseCode());
                return "{result:'fail',response:'errorCode:" + httpURLConnection.getResponseCode() + "'}";
            }
            System.out.println("nnn:code:"+httpURLConnection.getResponseCode());

            //判断请求成功
            if (httpURLConnection.getResponseCode() == 200) {
                //将服务器的数据转化返回到客户端
                InputStream inputStream = httpURLConnection.getInputStream();
               // InputStream outputStream = httpURLConnection.getOutputStream();

                byte[] bytes = new byte[0];
                bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                output = new String(bytes);
                inputStream.close();
                System.out.println("nnn:ok");
            }
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
            //logger.error(url + "   请求异常，错误信息为：  " + e.getMessage());
            return "{result:'fail',response:'" + e.getMessage() + "'}";
        }
        return output;
    }
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }


    public static String doPostSubmitBody(String url, Map<String, String> map,
                                          String filePath, byte[] body_data, String charset) {
        // 设置三个常用字符串常量：换行、前缀、分界线（NEWLINE、PREFIX、BOUNDARY）；
        final String NEWLINE = "\r\n";
        final String PREFIX = "--";
        final String BOUNDARY = "#";
        HttpURLConnection httpConn = null;
        BufferedInputStream bis = null;
        DataOutputStream dos = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // 实例化URL对象。调用URL有参构造方法，参数是一个url地址；
            URL urlObj = new URL(url);
            // 调用URL对象的openConnection()方法，创建HttpURLConnection对象；
            httpConn = (HttpURLConnection) urlObj.openConnection();
            // 调用HttpURLConnection对象setDoOutput(true)、setDoInput(true)、setRequestMethod("POST")；
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);
            httpConn.setRequestMethod("POST");
            // 设置Http请求头信息；（Accept、Connection、Accept-Encoding、Cache-Control、Content-Type、User-Agent）
            httpConn.setUseCaches(false);
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("Accept", "*/*");
            httpConn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            httpConn.setRequestProperty("Cache-Control", "no-cache");
            httpConn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + BOUNDARY);
            httpConn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)");
            // 调用HttpURLConnection对象的connect()方法，建立与服务器的真实连接；
            httpConn.connect();

            // 调用HttpURLConnection对象的getOutputStream()方法构建输出流对象；
            dos = new DataOutputStream(httpConn.getOutputStream());
            // 获取表单中上传控件之外的控件数据，写入到输出流对象（根据HttpWatch提示的流信息拼凑字符串）；
            if (map != null && !map.isEmpty()) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String key = entry.getKey();
                    String value = map.get(key);
                    dos.writeBytes(PREFIX + BOUNDARY + NEWLINE);
                    dos.writeBytes("Content-Disposition: form-data; "
                            + "name=\"" + key + "\"" + NEWLINE);
                    dos.writeBytes(NEWLINE);
                    dos.writeBytes(URLEncoder.encode(value.toString(), charset));
                    // 或者写成：dos.write(value.toString().getBytes(charset));
                    dos.writeBytes(NEWLINE);
                }
            }


            // 获取表单中上传控件的数据，写入到输出流对象（根据HttpWatch提示的流信息拼凑字符串）；
            if (body_data != null && body_data.length > 0) {
                dos.writeBytes(PREFIX + BOUNDARY + NEWLINE);
                String fileName = filePath.substring(filePath
                        .lastIndexOf(File.separatorChar));
                dos.writeBytes("Content-Disposition: form-data; " + "name=\""
                        + "file" + "\"" + "; filename=\"" + fileName
                        + "\"" + NEWLINE);
                dos.writeBytes(NEWLINE);
                dos.write(body_data);
                dos.writeBytes(NEWLINE);
            }
            dos.writeBytes(PREFIX + BOUNDARY + PREFIX + NEWLINE);
            dos.flush();

            // 调用HttpURLConnection对象的getInputStream()方法构建输入流对象；
            byte[] buffer = new byte[8 * 1024];
            int c = 0;
            // 调用HttpURLConnection对象的getResponseCode()获取客户端与服务器端的连接状态码。如果是200，则执行以下操作，否则返回null；
            if (httpConn.getResponseCode() == 200) {
                bis = new BufferedInputStream(httpConn.getInputStream());
                while ((c = bis.read(buffer)) != -1) {
                    baos.write(buffer, 0, c);
                    baos.flush();
                }
            }
            // 将输入流转成字节数组，返回给客户端。
            return new String(baos.toByteArray(), charset);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                dos.close();
                bis.close();
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }



    /**
     *      * 将输入流转化成字节流
     *      * @param inStream
     *      * @return
     *      * @throws IOException
     *      
     */
    public static final byte[] input2byte(InputStream inStream) throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }

}
