package com.dongbao.voa51.utils;

import android.util.Log;

import com.dongbao.voa51.MetaData;
import com.dongbao.voa51.notification.ProgressNotification;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by 15018 on 2017/5/25.
 */

public class HttpDownloader {

    static String TAG = "HttpDownloader";

    /**
     * 代码来源：http://www.cnblogs.com/h--d/p/5638092.html
     * @param urlPath 下载路径
     * @param saveDir 下载存放目录
     * @return 返回下载文件
     */
    public static File downloadFile(String urlPath, String saveDir, String fileName) {
        if (MetaData.LOG_ON) Log.d(TAG, String.format("HttpDownloader.downloadFile\nurl:%s\nsave:%s\nfile:%s", urlPath, saveDir, fileName));
        File file = null;
        ProgressNotification progressNotification = null;
        if (MetaData.SHOW_NOTI)
            progressNotification = new ProgressNotification(Publics.self, fileName);
        try {
            // 统一资源
            URL url = new URL(urlPath);
            // 连接类的父类，抽象类
            URLConnection urlConnection = url.openConnection();
            // http的连接类
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            // 设定请求的方法，默认是GET
//            httpURLConnection.setRequestMethod("POST");
            // 设置字符编码
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            // 打开到此 URL 引用的资源的通信链接（如果尚未建立这样的连接）。
            httpURLConnection.connect();

            // 文件大小
            int fileLength = httpURLConnection.getContentLength();
            Log.d(TAG, "file length---->" + fileLength);

            // 文件名
            String filePathUrl = httpURLConnection.getURL().getFile();
//            String fileFullName = filePathUrl.substring(filePathUrl.lastIndexOf("/") + 1);

            URLConnection con = url.openConnection();

            BufferedInputStream bin = new BufferedInputStream(httpURLConnection.getInputStream());

//            String path = saveDir + File.separatorChar + fileFullName;
            String path = saveDir + File.separatorChar + fileName;
            //System.out.println(path);
            file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            OutputStream out = new FileOutputStream(file);
            int size;
            int len = 0;
            byte[] buf = new byte[1024];
            int previousProgress = 0;
            while ((size = bin.read(buf)) != -1) {
                len += size;
                out.write(buf, 0, size);
                // 通知下载百分比
                int progress = len * 100 / fileLength;
                if (progress > previousProgress) {
                    previousProgress = progress;
                    if (MetaData.SHOW_NOTI && progressNotification != null)
                        progressNotification.setProgress(progress);
                }
            }
            bin.close();
            out.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            if (MetaData.SHOW_NOTI && progressNotification != null)
                progressNotification.closeNotification();
            return file;
        }
    }
}
