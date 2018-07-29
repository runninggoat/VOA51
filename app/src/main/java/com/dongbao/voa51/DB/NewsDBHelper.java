package com.dongbao.voa51.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.dongbao.voa51.MetaData;

public class NewsDBHelper extends SQLiteOpenHelper {

    private final String TAG = "NewsDBHelper";

    public static String NEWS_ID = "news_id";
    public static String NEWS_DB_TABLE_NAME = "news_db";
    public static String DATE_STRING = "date_string";
    public static String NEWS_JSON = "news_json";
    private static String CREATE_NEWS_DB_SQL = "CREATE TABLE "+ NEWS_DB_TABLE_NAME +" (" + NEWS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DATE_STRING + " text not null, " + NEWS_JSON +" text);";

    public NewsDBHelper(Context context, String name,
                        SQLiteDatabase.CursorFactory
                                factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        if (MetaData.LOG_ON) Log.d(TAG, "onCreate news database...");
        sqLiteDatabase.execSQL(NewsDBHelper.CREATE_NEWS_DB_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase,
                          int i, int i1) {
        if (MetaData.LOG_ON) Log.d(TAG, "onUpgrade news database...");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + NEWS_DB_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
