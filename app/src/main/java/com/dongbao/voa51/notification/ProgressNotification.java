package com.dongbao.voa51.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.dongbao.voa51.MetaData;
import com.dongbao.voa51.R;

/**
 * Created by 15018 on 2017/7/2.
 */

public class ProgressNotification {

    private static String TAG = "ProgressNotification";
    String title;
    NotificationManager notificationManager;
    NotificationCompat.Builder builder;
    static final int NOTIFICATION_ID = 0;

    public ProgressNotification(Context context, String newsTitle) {
        if(MetaData.LOG_ON) Log.i(TAG, "ProgressNotification.ProgressNotification(constructor)");
        title = newsTitle;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(context);

        //必须要设置小图标，否则出错
        builder.setSmallIcon(R.drawable.download);

        // 这个参数是通知提示闪出来的值.
        builder.setContentTitle(String.format("开始下载: %s", title));

        // 这里面的参数是通知栏view显示的内容
        builder.setContentText(String.format("下载：%s", "0.00") + "%");

        //设置通知不被清除
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void setProgress(int progress) {
        // 这里面的参数是通知栏view显示的内容
        builder.setContentText(String.format("下载：%s", String.valueOf(progress)) + "%");

        //设置通知不被清除
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void closeNotification() {
        if(MetaData.LOG_ON) Log.i(TAG, "ProgressNotification.closeNotification");
        notificationManager.cancel(NOTIFICATION_ID);
    }

}
