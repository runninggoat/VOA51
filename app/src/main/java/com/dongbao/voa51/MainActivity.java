package com.dongbao.voa51;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.*;

import com.dongbao.voa51.DB.NewsDBHelper;
import com.dongbao.voa51.models.Album;
import com.dongbao.voa51.models.News;
import com.dongbao.voa51.utils.FileManager;
import com.dongbao.voa51.utils.Publics;
import com.dongbao.voa51.utils.VOA51Crawler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String INDEX_URL = "http://www.51voa.com";

    private final Uri uri = Uri.parse("content://news/news"); //news_db的URI

    private ContentResolver contentResolver = null; //DB的操作实例

    private ExpandableListView expandableListView; //可以折叠或展开的列表
    private ProgressBar progressBar; //一个不停转的进度条，加载的时候显示，加载完成时隐藏

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (MetaData.LOG_ON) Log.d(TAG, "onCreate...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Publics.self = this;

        expandableListView =(ExpandableListView)findViewById(R.id.expandable_listview);
        progressBar = (ProgressBar) this.findViewById(R.id.progress_bar);

        contentResolver = getContentResolver();
    }

    @Override
    protected void onResume() {
        if (MetaData.LOG_ON) Log.d(TAG, "onResume...");
        //onCreate之后onResume，在这里调用初始化列表的方法
        InitListTask initListTask = new InitListTask();
        initListTask.execute(INDEX_URL, InitListTask.LOCAL);
        super.onResume();
    }

    /**
     * 内部类，初始化列表的任务，主要有2种方式
     * 1、本地缓存数据，如果完全没有数据，则访问首页获取数据
     * 2、访问首页获取数据，并将还未缓存的数据添加到数据库中
     */
    private class InitListTask extends AsyncTask<String, Void, Void> {

        private static final String TAG = "InitListTask";

        private List<Album> albumList;
        private boolean hasCache;

        private static final String LOCAL = "local";
        private static final String NETWORK = "network";

        @Override
        protected Void doInBackground(String... strings) {
            if (MetaData.LOG_ON) Log.d(TAG, String.format("InitListTask.doInBackground\n%s\n%s", strings[0], strings[1]));
            String columns[] = new String[] {NewsDBHelper.NEWS_ID, NewsDBHelper.DATE_STRING, NewsDBHelper.NEWS_JSON};
            //先查询数据库中所有数据，若无数据，进行一次访问，加入新数据
            hasCache = true;
            Cursor cursor = contentResolver.query(uri, columns, null, null,NewsDBHelper.DATE_STRING + " DESC");
            albumList = getAlbumListFromDB(cursor);
            if (albumList != null) {
                if (MetaData.LOG_ON) Log.d(TAG, "DB has cache!");
            } else {
                hasCache = false;
            }
            if (!hasCache) {
                //没有缓存数据则访问首页获取数据
                if (MetaData.LOG_ON) Log.d(TAG, "No cache! Request for new data from " + strings[0]);
                albumList = VOA51Crawler.crawlIndexPage(strings[0]);
                if (albumList != null) {
                    for (Album album : albumList) {
                        for (News news : album.getNewsList()) {
                            ContentValues values = new ContentValues();
                            values.put(NewsDBHelper.DATE_STRING, news.getDateString());
                            values.put(NewsDBHelper.NEWS_JSON, Publics.gson.toJson(news));
                            contentResolver.insert(uri, values);
                        }
                    }
                } else {
                    Log.e(TAG, "访问网站失败");
                    Toast.makeText(Publics.self, "访问网站失败", Toast.LENGTH_LONG).show();
                }
                cursor = contentResolver.query(uri, columns, null, null,NewsDBHelper.DATE_STRING + " DESC");
                albumList = getAlbumListFromDB(cursor);
            }
            if (NETWORK.equals(strings[1])) {
                //选择强制从首页获取新的信息
                if (MetaData.LOG_ON) Log.d(TAG, "Request for new data from " + strings[0]);
                List<Album> albumListFromNetwork = VOA51Crawler.crawlIndexPage(strings[0]);
                Set<String> newsTitleSet = new HashSet<>();
                if (albumList != null) {
                    for (Album album : albumList) {
                        for (News news : album.getNewsList()) {
                            newsTitleSet.add(news.getTitle());
                        }
                    }
                    for (Album album : albumListFromNetwork) {
                        for (News news : album.getNewsList()) {
                            if (!newsTitleSet.contains(news.getTitle())) {
                                newsTitleSet.add(news.getTitle());
                                ContentValues values = new ContentValues();
                                values.put(NewsDBHelper.DATE_STRING, news.getDateString());
                                values.put(NewsDBHelper.NEWS_JSON, Publics.gson.toJson(news));
                                contentResolver.insert(uri, values);
                            }
                        }
                    }
                } else {
                    for (Album album : albumListFromNetwork) {
                        for (News news : album.getNewsList()) {
                            ContentValues values = new ContentValues();
                            values.put(NewsDBHelper.DATE_STRING, news.getDateString());
                            values.put(NewsDBHelper.NEWS_JSON, Publics.gson.toJson(news));
                            contentResolver.insert(uri, values);
                        }
                    }
                }
                //按日期字符的降序查出新闻信息，最新的信息会出现在列表头
                cursor = contentResolver.query(uri, columns, null, null,NewsDBHelper.DATE_STRING + " DESC");
                albumList = getAlbumListFromDB(cursor);
            }
//            for (Album album : albumList) {
//                if (MetaData.LOG_ON) Log.d(TAG, album.getDateString());
//                for (News news : album.getNewsList()) {
//                    if (MetaData.LOG_ON) Log.d(TAG, String.format("%d %s", news.getNews_id(), news.getTitle()));
//                }
//            }
            return null;
        }

        /**
         * 从数据库中读取缓存数据
         * @param cursor 查询数据得到的游标，利用游标获取数据
         * @return 返回合集列表
         */
        private List<Album> getAlbumListFromDB(Cursor cursor) {
            List<Album> albumList = null;
            if (MetaData.LOG_ON) Log.d(TAG, "Read data from DB.");
            if (cursor.moveToFirst()) {
                albumList = new ArrayList<>();
                Map<String, Integer> indexMap = new HashMap<>();
                do {
                    String jsonStr = cursor.getString(cursor.getColumnIndex(NewsDBHelper.NEWS_JSON));
                    //读取新闻信息在数据库中的id
                    int news_id = Integer.valueOf(cursor.getString(cursor.getColumnIndex(NewsDBHelper.NEWS_ID)));
                    News news = Publics.gson.fromJson(jsonStr, News.class);
                    news.setNews_id(news_id);
                    //整理数据
                    if (indexMap.containsKey(news.getDateString())) {
                        albumList.get(indexMap.get(news.getDateString())).addNews(news);
                    } else {
                        Album album = new Album(news.getDateString());
                        album.addNews(news);
                        albumList.add(album);
                        indexMap.put(album.getDateString(), albumList.size() - 1);
                    }
                } while (cursor.moveToNext());
            } else {
                if (MetaData.LOG_ON) Log.d(TAG, "DB has no cache!");
            }
            return albumList;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (hasCache) {
                Toast.makeText(Publics.self, "已有缓存数据，最新数据请刷新", Toast.LENGTH_LONG).show();
            }
            //隐藏环形转动进度条
            ViewGroup viewGroup = (ViewGroup) progressBar.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(progressBar);
            }
            //将数据加载到可折叠展开的列表中
            ExpandableListViewAdapter expandableListViewAdapter = new ExpandableListViewAdapter();
            expandableListViewAdapter.setAlbumList(albumList);
            expandableListView.setAdapter(expandableListViewAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, 1, 1, "清空所有日期数据");
        menu.add(2, 2, 2, "更新数据");
//        menu.add(3, 3, 3, "删除今日数据");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case 1:
                if (MetaData.LOG_ON) Log.d(TAG, "清空所有日期数据!");
                Toast.makeText(this, "清空所有日期数据", Toast.LENGTH_LONG).show();
                contentResolver.delete(uri, null, null);
                File filePath = this.getFilesDir();
                for (File f : filePath.listFiles()) {
                    FileManager.clearFile(f);
                }
                break;
            case 2:
                if (MetaData.LOG_ON) Log.d(TAG, "访问网络更新数据!");
                Toast.makeText(this, "访问网络更新数据", Toast.LENGTH_LONG).show();
                InitListTask initListTask = new InitListTask();
                initListTask.execute(INDEX_URL, InitListTask.NETWORK);
                break;
//            case 3:
//                if (MetaData.LOG_ON) Log.d(TAG, "删除今日数据测试!");
//                Toast.makeText(this, "删除今日数据测试", Toast.LENGTH_LONG).show();
//                Date date = new Date();
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-M-d");
//                String dateString = simpleDateFormat.format(date);
//                contentResolver.delete(uri, NewsDBHelper.DATE_STRING + "=?", new String[]{dateString});
//                initListTask = new InitListTask();
//                initListTask.execute(INDEX_URL, InitListTask.LOCAL);
//                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

