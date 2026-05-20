package com.example.myapplication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;


public class HTTPAPI {

    /**
     * POST 请求（application/x-www-form-urlencoded）
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();

        try {
            URLConnection conn = new URL(url).openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            System.out.println("HTTP POST 异常: " + e.getMessage());
        } finally {
            try { if (out != null) out.close(); } catch (Exception ignored) {}
            try { if (in != null) in.close(); } catch (Exception ignored) {}
        }
        return result.toString();
    }

    /**
     * POST 文件上传（multipart/form-data）
     */
    public static String uploadFile(String url, Map<String, String> params, File file) {
        final String NEWLINE = "\r\n";
        final String PREFIX = "--";
        final String BOUNDARY = "----WebKitFormBoundaryCXRtmcVNK0H70msG";
        StringBuilder output = new StringBuilder();
        HttpURLConnection conn = null;

        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data;charset=UTF-8");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.connect();

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    dos.writeBytes(PREFIX + BOUNDARY + NEWLINE);
                    dos.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + NEWLINE);
                    dos.writeBytes(NEWLINE);
                    dos.writeBytes(entry.getValue() != null ? entry.getValue() : "");
                    dos.writeBytes(NEWLINE);
                }
            }

            if (file != null && file.exists()) {
                dos.writeBytes(PREFIX + BOUNDARY + NEWLINE);
                dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"" + NEWLINE);
                dos.writeBytes(NEWLINE);
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
                fis.close();
                dos.writeBytes(NEWLINE);
            }

            dos.writeBytes(PREFIX + BOUNDARY + PREFIX + NEWLINE);
            dos.flush();
            dos.close();

            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream is = conn.getInputStream();
                byte[] bytes = new byte[is.available()];
                is.read(bytes);
                output.append(new String(bytes));
                is.close();
            } else {
                return "{result:'fail', response:'errorCode:" + code + "'}";
            }
        } catch (Exception e) {
            return "{result:'fail', response:'" + e.getMessage() + "'}";
        } finally {
            if (conn != null) conn.disconnect();
        }
        return output.toString();
    }
}
