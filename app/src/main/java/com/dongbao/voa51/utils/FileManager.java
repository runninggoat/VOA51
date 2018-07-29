package com.dongbao.voa51.utils;

import android.content.Context;
import android.util.Log;

import com.dongbao.voa51.models.News;

import java.io.File;

/**
 * Created by 15018 on 2017/5/26.
 */

public class FileManager {

    static String TAG = "File manager";

    public static boolean dateDirExist(Context context, String dateString) {
        File dir = new File(context.getFilesDir() + File.separator + dateString + File.separator);
        return dir.exists();
    }

    public static boolean newsAudioExist(Context context, String dateString, News news) {
        File file = new File(context.getFilesDir() + File.separator + dateString, news.getTitle() + ".mp3");
        return file.exists();
    }

    public static File getNewsAudioContentLocation(Context context, String dateString, News news) {
        if (newsAudioExist(context, dateString, news)) {
            return new File(context.getFilesDir() + File.separator + dateString, news.getTitle() + ".mp3");
        }
        return null;
    }

    public static void clearFile(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                clearFile(f);
            }
            Log.w(TAG, "delete dir: " + file.getAbsolutePath());
            file.delete();
        } else {
            if (file.getName().equals("indexlist.json")) return; //放过这个json文件
            Log.w(TAG, "delete file: " + file.getAbsolutePath());
            file.delete();
        }
    }

}
