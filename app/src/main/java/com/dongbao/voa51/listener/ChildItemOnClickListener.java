package com.dongbao.voa51.listener;

import android.content.Intent;
import android.view.View;

import com.dongbao.voa51.MetaData;
import com.dongbao.voa51.PlayerActivity;
import com.dongbao.voa51.models.Album;
import com.dongbao.voa51.utils.Publics;

public class ChildItemOnClickListener implements View.OnClickListener {

    private Album album;
    private int currentAudioIndex;

    public ChildItemOnClickListener(Album a, int index) {
        album = a;
        currentAudioIndex = index;
    }

    @Override
    public void onClick(View view) {
        //点击子条目时启动PlayerActivity
        Intent intent = new Intent();
        intent.putExtra(MetaData.ALBUM, album);
        intent.putExtra(MetaData.CURRENT_AUDIO_INDEX, currentAudioIndex);
        intent.setClass(Publics.self, PlayerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Publics.self.startActivity(intent);
    }
}
