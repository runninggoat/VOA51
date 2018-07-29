package com.dongbao.voa51;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dongbao.voa51.models.Album;
import com.dongbao.voa51.utils.Publics;

public class PlayerActivity extends AppCompatActivity {

    private static String TAG = "PlayerActivity";

    private ImageButton playButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private TextView newsTitle;
    private TextView text;
    private SeekBar seekBar;
    private TextView timeText;

    private Album album;
    private int currentAudioIndex;

    private boolean connected;

    private int mode = Mp3PlayService.SEQ_MODE;

    private Mp3PlayService mp3PlayService;

    public static final int EXIT_PLAYER = -1; //退出播放器
    public static final int UPDATE_STATUS = 1; //更新播放进度条
    public static final int UPDATE_TITLE_CONTENT = 2; //更新标题和内容段落
    public static final int UPDATE_TO_PLAYING_STATE = 3; //更新播放按钮为播放中的状态，即pause.png
    public static final int UPDATE_TO_PAUSE_STATE = 4; //更新播放按钮为暂停的状态，即play.png
    public static final int ENABLE_BUTTONS = 5; //使按钮监听点击动作
    public static final int DISABLE_PLAYING = 6; //使按钮和播放进度条失去监听

    public static final String TITLE = "title";
    public static final String TEXT = "text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (MetaData.LOG_ON) Log.d(TAG, "onCreate...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playButton = (ImageButton) this.findViewById(R.id.play_btn);
        previousButton = (ImageButton) this.findViewById(R.id.previous);
        nextButton = (ImageButton) this.findViewById(R.id.next);
        newsTitle = (TextView) this.findViewById(R.id.current_news_name);
        text = (TextView) this.findViewById(R.id.text);
        seekBar = (SeekBar) this.findViewById(R.id.seek_bar);
        timeText = (TextView) this.findViewById(R.id.play_time);

        //获得传过来的Intent
        Intent intent = this.getIntent();
        if (intent != null) {
            album = (Album) intent.getSerializableExtra(MetaData.ALBUM);
            currentAudioIndex = intent.getIntExtra(MetaData.CURRENT_AUDIO_INDEX, 0);
        } else {
            Toast.makeText(this, "Intent为null", Toast.LENGTH_SHORT).show();
            this.finish();
        }

        if (MetaData.LOG_ON) Log.d(TAG, album.getDateString());

        //通过bindService连接播放服务
        Intent intent_ = new Intent();
        intent_.setClass(this, Mp3PlayService.class);
        connected = this.getApplicationContext().bindService(intent_, mServiceConnection, Service.BIND_AUTO_CREATE);
        if (MetaData.LOG_ON) Log.d(TAG, "bind service result: " + connected);

    }

    //此handler传递到service中，供service发送信息，让PlayerActivity执行相应动作
    public Handler mHandler = new Mp3PlayHandler();
    public class Mp3PlayHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EXIT_PLAYER:
                    //关掉这个Activity
                    exitPlayerActivity();
                    break;
                case UPDATE_STATUS:
                    seekBar.setProgress(msg.arg1);
                    timeText.setText(Publics.milliseconds2TimeString(msg.arg1));
                    break;
                case UPDATE_TITLE_CONTENT:
                    newsTitle.setText(msg.getData().getCharSequence(TITLE));
                    text.setText(msg.getData().getCharSequence(TEXT));
                    seekBar.setMax(msg.arg1);
                    break;
                case UPDATE_TO_PLAYING_STATE:
                    playButton.setImageResource(R.drawable.pause);
                    break;
                case UPDATE_TO_PAUSE_STATE:
                    playButton.setImageResource(R.drawable.play);
                    break;
                case ENABLE_BUTTONS:
                    playButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mp3PlayService.switchStatus();
                        }
                    });
                    previousButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mp3PlayService.previous();
                        }
                    });
                    nextButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mp3PlayService.next();
                        }
                    });
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                        private boolean touching = false;

                        @Override
                        public void onProgressChanged (SeekBar seekBar, int i, boolean b) {
                            if (!touching) {
                                seekBar.setProgress(i);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                            touching = true;
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            mp3PlayService.seekToPlay(seekBar.getProgress());
                            touching = false;
                        }
                    });
                    break;
                case DISABLE_PLAYING:
                    newsTitle.setText("正在准备中...");
                    text.setText("");
                    playButton.setOnClickListener(null);
                    previousButton.setOnClickListener(null);
                    nextButton.setOnClickListener(null);
                    seekBar.setOnSeekBarChangeListener(null);
                    break;
                default:
                    if (MetaData.LOG_ON) Log.d(TAG, "unsupport event: " + msg.what);
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        //控制本Activity的返回键监听，不让PlayerActivity被返回键销毁，无动作，仅弹出提示
        Toast.makeText(this, "要结束播放请先停止播放", Toast.LENGTH_SHORT).show();
//        super.onBackPressed();
    }

    private void exitPlayerActivity() {
        this.finish();
    }

    //service connection，用于连接服务
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //连接服务后的初始化动作
            if (MetaData.LOG_ON) Log.d(TAG, "onServiceConnected()...");
            mp3PlayService = ((Mp3PlayService.InternalBinder) iBinder).getService();
            mp3PlayService.setPlayingIndex(currentAudioIndex);
            mp3PlayService.setAlbum(album);
            mp3PlayService.setHandler(mHandler);
            mp3PlayService.startPlaying();
//            String title = mp3PlayService.getPlayingTitle();
            String title = "正在准备中...";
            newsTitle.setText(title);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (MetaData.LOG_ON) Log.d(TAG, "onServiceDisconnected()...");
        }
    };

//
//    @Override
//    protected void onPause() {
//        Log.w(TAG, "on pause triggered...");
//        super.onPause();
//    }
//
//    @Override
//    protected void onStop() {
//        Log.w(TAG, "on stop triggered...");
//        super.onStop();
//    }

    @Override
    protected void onDestroy() {
//        Log.w(TAG, "on destroy triggered...");

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, 1, 1, "停止播放");
        menu.add(2, 2, 2, mode == Mp3PlayService.SEQ_MODE ? "目前：顺序播放" : "目前：单曲循环");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case 1:
                Toast.makeText(this, "停止播放", Toast.LENGTH_SHORT).show();

                //关掉播放服务
                this.getApplicationContext().unbindService(mServiceConnection);
                break;
            case 2:
                if (mode == Mp3PlayService.SEQ_MODE) {
                    mode = Mp3PlayService.CYCLE_MODE;
                    Toast.makeText(this, "单曲循环", Toast.LENGTH_SHORT).show();
                } else {
                    mode = Mp3PlayService.SEQ_MODE;
                    Toast.makeText(this, "顺序播放", Toast.LENGTH_SHORT).show();
                }
                mp3PlayService.setMode(mode);
                invalidateOptionsMenu();
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.removeItem(2);
        if (mode == Mp3PlayService.SEQ_MODE) {
            menu.add(2, 2, 2, "目前：顺序播放");
        } else {
            menu.add(2, 2, 2, "目前：单曲循环");
        }
        return super.onPrepareOptionsMenu(menu);
    }
}
