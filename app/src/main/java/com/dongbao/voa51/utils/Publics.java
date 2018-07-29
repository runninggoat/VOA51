package com.dongbao.voa51.utils;

import android.app.Activity;
import android.content.Context;

import com.google.gson.Gson;

import java.text.DecimalFormat;

/**
 * 公共类，放一些工具方法，并提供一些静态实例
 */
public class Publics {

    /**
     * 将毫秒数转成##:##的字符串格式，由于精确到秒，剩余的毫秒数将被忽略
     * @param milliseconds 毫秒数
     * @return ##:##的字符串格式
     */
    public static String milliseconds2TimeString (int milliseconds) {
        DecimalFormat df = new DecimalFormat("##");
        int minutes = 0;
        int seconds = 0;
        minutes = milliseconds / (60 * 1000);
        milliseconds = milliseconds % (60 * 1000);
        seconds = milliseconds / (1000);
        return df.format(minutes)+":"+df.format(seconds);
    }

    /**
     * Gson实例，可供不同的实例做JSON数据互转的时候使用
     */
    public static Gson gson = new Gson();

    /**
     * 本应用的环境引用，供其他子环境引用
     */
    public static Activity self;

}
