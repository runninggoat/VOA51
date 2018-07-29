package com.dongbao.voa51.DB;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.dongbao.voa51.MetaData;

import java.util.*;

public class NewsContentProvider extends ContentProvider {

    private final String TAG = "NewsContentProvider";
    private SQLiteDatabase sqLiteDatabase;
    private NewsDBHelper newsDBHelper;

    //定义设置数据表中的两列列名信息
    private static HashMap<String, String> fieldMap = new HashMap<>();
    static {
        fieldMap.put(NewsDBHelper.NEWS_ID, NewsDBHelper.NEWS_ID);
        fieldMap.put(NewsDBHelper.DATE_STRING, NewsDBHelper.DATE_STRING);
        fieldMap.put(NewsDBHelper.NEWS_JSON, NewsDBHelper.NEWS_JSON);
    }

    public NewsContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (MetaData.LOG_ON) Log.d(TAG, "news provider delete call");
        sqLiteDatabase =  newsDBHelper.getWritableDatabase();
        return sqLiteDatabase.delete(NewsDBHelper.NEWS_DB_TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        return NewsDBHelper.NEWS_DB_TABLE_NAME;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (MetaData.LOG_ON) Log.d(TAG, "news provider insert call");
        sqLiteDatabase = newsDBHelper.getWritableDatabase();
        long id = sqLiteDatabase.insert(NewsDBHelper.NEWS_DB_TABLE_NAME, NewsDBHelper.NEWS_ID, values);
        if(id < 0) {
            throw new SQLiteException("Unable to insert " + values + " for " + uri);
        }
        Uri newUri = ContentUris.withAppendedId(uri, id);
        return newUri;
    }

    @Override
    public boolean onCreate() {
        newsDBHelper = new NewsDBHelper(getContext(), NewsDBHelper.NEWS_DB_TABLE_NAME, null, 1);
        if (MetaData.LOG_ON) Log.d(TAG, "news provider created.");
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        if (MetaData.LOG_ON) Log.d(TAG, "news provider query call");
        sqLiteDatabase = newsDBHelper.getReadableDatabase();
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(NewsDBHelper.NEWS_DB_TABLE_NAME);
        sqLiteQueryBuilder.setProjectionMap(fieldMap);
        Cursor cursor = sqLiteQueryBuilder.query(sqLiteDatabase, projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values,
                      String selection,
                      String[] selectionArgs) {
        if (MetaData.LOG_ON) Log.d(TAG, "news provider update");
        sqLiteDatabase = newsDBHelper.getWritableDatabase();
        int count = 0;
        count = sqLiteDatabase.update(NewsDBHelper.NEWS_DB_TABLE_NAME, values, selection, selectionArgs);
        return count;
    }
}
