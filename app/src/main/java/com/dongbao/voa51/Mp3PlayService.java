package com.dongbao.voa51;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.dongbao.voa51.DB.NewsDBHelper;
import com.dongbao.voa51.models.Album;
import com.dongbao.voa51.models.News;
import com.dongbao.voa51.utils.Publics;
import com.dongbao.voa51.utils.VOA51Crawler;

import java.io.File;
import java.io.IOException;

public class Mp3PlayService extends Service {

    private static String TAG = "Mp3PlayService";

    public static final int SEQ_MODE = 1;
    public static final int CYCLE_MODE = 2;

    private int mode = SEQ_MODE;

    private Handler mHandler = null;

    private MediaPlayer mediaPlayer;
    private Album album;
    private int playingIndex;
    private UpdateProgress updateProgress;

    private final Uri uri = Uri.parse("content://news/news");
    private ContentResolver contentResolver;

    private News updatedNews;

    public Mp3PlayService() {
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * 供PlayerActivity传递handler，获得handler之后可以向PlayerActivity发送信息命令
     * @param h
     */
    public void setHandler(Handler h) {
        if (MetaData.LOG_ON) Log.d(TAG, "setHandler()...");
        mHandler = h;
    }

    public String getPlayingTitle() {
        return album.getNewsAudioAtPosition(playingIndex).getTitle();
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public int getPlayingIndex() {
        return playingIndex;
    }

    public void setPlayingIndex(int playingIndex) {
        this.playingIndex = playingIndex;
    }

    /**
     * 根据playingIndex指向的新闻进行播放任务
     */
    public void startPlaying() {
        if (MetaData.LOG_ON) Log.d(TAG, "start playing...");
        News news = album.getNewsAudioAtPosition(playingIndex);
        //检查这条新闻信息实例里面是否有正确的播放信息
        try {
            checkURL(news);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查新闻信息的播放信息
     * @param news 需要播放的新闻
     */
    private void checkURL(News news) {
        updatedNews = news;
        if (MetaData.LOG_ON) Log.d(TAG, "news audio url: " + news.getAudioUrl() + "; news local uri: " + news.getLocalMp3Uri());
        if (news.getAudioUrl() == null || news.getAudioUrl().isEmpty() || news.getLocalMp3Uri() == null || news.getLocalMp3Uri().isEmpty()) {
            //音频文件在线地址或本地路径为空的时候运行异步任务访问新闻信息页面更新信息
            //如果不通过异步任务访问网络，系统会禁止网络访问
            CrawlNewsPageTask crawlNewsPageTask = new CrawlNewsPageTask();
            crawlNewsPageTask.execute(updatedNews);
        } else {
            onPostCheckURL();
        }
    }

    /**
     * 播放信息检查完毕，可以开始播放
     */
    private void onPostCheckURL() {
        if (MetaData.LOG_ON) Log.d(TAG, "play uri: "+ updatedNews.getLocalMp3Uri());
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer = MediaPlayer.create(this.getApplicationContext(), Uri.parse("file://" + updatedNews.getLocalMp3Uri()));
            } else {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(this.getApplicationContext(), Uri.parse("file://" + updatedNews.getLocalMp3Uri()));
            }
            mediaPlayer.prepare();
            mediaPlayer.seekTo(0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if (MetaData.LOG_ON) Log.d(TAG, "play complete...");
                    if (mode == CYCLE_MODE) {
                        mediaPlayer.seekTo(0);
                        mediaPlayer.start();
                    } else next();
                }
            });
            //更新播放中的新闻的题目和段落内容
            Message m = mHandler.obtainMessage();
            m.what = PlayerActivity.UPDATE_TITLE_CONTENT;
            Bundle bundle = new Bundle();
            bundle.putCharSequence(PlayerActivity.TITLE, updatedNews.getTitle());
            bundle.putCharSequence(PlayerActivity.TEXT, updatedNews.getContent());
            m.setData(bundle);
            m.arg1 = mediaPlayer.getDuration();
            m.sendToTarget();
            //更新按钮的状态，由于正在播放，因此将状态设置成播放状态
            m = mHandler.obtainMessage();
            m.what = PlayerActivity.UPDATE_TO_PLAYING_STATE;
            m.sendToTarget();
            //已经准备好播放让按钮的功能生效
            m = mHandler.obtainMessage();
            m.what = PlayerActivity.ENABLE_BUTTONS;
            m.sendToTarget();
        } else {
            Toast.makeText(this, "播放失败", Toast.LENGTH_LONG).show();
            updateProgress.terminate();
            Message m = mHandler.obtainMessage();
            m.what = PlayerActivity.EXIT_PLAYER;
            m.sendToTarget();
        }
    }

    private void popMessage(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    /**
     * 访问新闻信息页面更新新闻信息实例的异步任务
     */
    class CrawlNewsPageTask extends AsyncTask<News, Void, Void> {

        @Override
        protected Void doInBackground(News... args) {
            updatedNews = VOA51Crawler.crawlNewsPage(args[0]);
            if (MetaData.LOG_ON) Log.d(TAG, "check audio url: " + updatedNews.getAudioUrl());
            if (updatedNews.getAudioUrl() == null || updatedNews.getAudioUrl().isEmpty()) {
                contentResolver.delete(uri, NewsDBHelper.NEWS_ID + "=?", new String[]{updatedNews.getNews_id() + ""});
                if (MetaData.LOG_ON) Log.d(TAG, "解析失败");
                updateProgress.terminate();
                Message m = mHandler.obtainMessage();
                m.what = PlayerActivity.EXIT_PLAYER;
                m.sendToTarget();
                return null;
            }
            File downloadedFile = VOA51Crawler.downloadMp3(updatedNews.getAudioUrl(), updatedNews.getDateString(), updatedNews.getTitle() + ".mp3");
            if (downloadedFile == null) {
                if (MetaData.LOG_ON) Log.d(TAG, "下载失败");
                updateProgress.terminate();
                Message m = mHandler.obtainMessage();
                m.what = PlayerActivity.EXIT_PLAYER;
                m.sendToTarget();
                return null;
            }
            //更新新闻信息实例
            updatedNews.setLocalMp3Uri(downloadedFile.getAbsolutePath());
            ContentValues values = new ContentValues();
            values.put(NewsDBHelper.NEWS_ID, updatedNews.getNews_id());
            values.put(NewsDBHelper.DATE_STRING, updatedNews.getDateString());
            values.put(NewsDBHelper.NEWS_JSON, Publics.gson.toJson(updatedNews));
            contentResolver.update(uri, values, NewsDBHelper.NEWS_ID + "=?",  new String[]{updatedNews.getNews_id() + ""});
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            onPostCheckURL();
        }
    }

    /**
     * 切换播放状态
     */
    public void switchStatus() {
        if (mediaPlayer != null) {
            if (MetaData.LOG_ON) Log.d(TAG, "media player playing is: " + mediaPlayer.isPlaying());
            Message m = mHandler.obtainMessage();
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                m.what = PlayerActivity.UPDATE_TO_PAUSE_STATE;
            } else {
                mediaPlayer.start();
                m.what = PlayerActivity.UPDATE_TO_PLAYING_STATE;
            }
            m.sendToTarget();
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Message m = mHandler.obtainMessage();
            m.what = PlayerActivity.UPDATE_TO_PAUSE_STATE;
            m.sendToTarget();
        }
    }

    /**
     * 上一首
     */
    public void previous() {
        if (MetaData.LOG_ON) Log.d(TAG, "previous...");
        pause();
        Message m = mHandler.obtainMessage();
        m.what = PlayerActivity.DISABLE_PLAYING;
        m.sendToTarget();
        if (playingIndex <= 0) {
            playingIndex = album.getAlbumSize() - 1;
        } else {
            --playingIndex;
        }
        startPlaying();
    }

    /**
     * 下一首
     */
    public void next() {
        if (MetaData.LOG_ON) Log.d(TAG, "next...");
        pause();
        Message m = mHandler.obtainMessage();
        m.what = PlayerActivity.DISABLE_PLAYING;
        m.sendToTarget();
        if (playingIndex >= album.getAlbumSize() - 1) {
            playingIndex = 0;
        } else {
            ++playingIndex;
        }
        startPlaying();
    }

    /**
     * 拖动进度条后调整播放进度
     */
    public void seekToPlay(int t) {
        mediaPlayer.seekTo(t);
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (MetaData.LOG_ON) Log.d(TAG, "onBind()...");
        return new InternalBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (MetaData.LOG_ON) Log.d(TAG, "onUnbind()...");
        updateProgress.terminate();
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        if (MetaData.LOG_ON) Log.d(TAG, "onCreate()...");
        contentResolver = Publics.self.getContentResolver();
        playingIndex = 0;
        mediaPlayer = null;
        updateProgress = new UpdateProgress();
        updateProgress.start();
    }

    public class InternalBinder extends Binder {
        public Mp3PlayService getService() {
            return Mp3PlayService.this;
        }
    }

    /**
     * 不断地更新播放进度
     */
    class UpdateProgress extends Thread {

        private boolean stop;

        public UpdateProgress() {
            stop = false;
        }

        public void terminate() {
            stop = true;
        }

        @Override
        public void run() {
            for (;;) {
                if (stop) break;
                try {
                    if (mediaPlayer != null && mHandler != null) {
                        Message m = mHandler.obtainMessage();
                        m.what = PlayerActivity.UPDATE_STATUS;
                        m.arg1 = mediaPlayer.getCurrentPosition();
                        m.sendToTarget();
                    }
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            Message m = mHandler.obtainMessage();
            m.what = PlayerActivity.EXIT_PLAYER;
            m.sendToTarget();
        }
    }
}
