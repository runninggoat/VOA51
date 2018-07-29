package com.dongbao.voa51;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dongbao.voa51.listener.ChildItemOnClickListener;
import com.dongbao.voa51.models.*;
import com.dongbao.voa51.utils.Publics;

import java.util.List;

/**
 * Created by 15018 on 2017/5/25.
 */

public class ExpandableListViewAdapter extends BaseExpandableListAdapter {

    List<Album> albumList;

    public ExpandableListViewAdapter() {
    }

    public void setAlbumList(List<Album> albumList) {
        this.albumList = albumList;
    }

    @Override
    public int getGroupCount() {
        return albumList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return albumList.get(groupPosition).getAlbumSize();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return albumList.get(groupPosition).getDateString();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return albumList.get(groupPosition).getNewsAudioAtPosition(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Album album = albumList.get(groupPosition);
        String dateString = album.getDateString();
        //通过layout inflater利用xml布局文件产生view
        LayoutInflater layoutInflater = (LayoutInflater) Publics.self.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout relativeLayout = (RelativeLayout) layoutInflater.inflate(R.layout.group_layout, null);
        TextView textView = (TextView) relativeLayout.findViewById(R.id.name);
        textView.setText(dateString);
        return relativeLayout;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String name = albumList.get(groupPosition).getNewsAudioAtPosition(childPosition).getTitle();
        Album album = albumList.get(groupPosition);
        //通过layout inflater利用xml布局文件产生view
        LayoutInflater layoutInflater = (LayoutInflater) Publics.self.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout relativeLayout = (RelativeLayout) layoutInflater.inflate(R.layout.child_layout, null);
        TextView textView = (TextView) relativeLayout.findViewById(R.id.name);
        textView.setText(name);
        ImageView statusImg = relativeLayout.findViewById(R.id.status);
        ViewGroup.LayoutParams layoutParams = statusImg.getLayoutParams();
        layoutParams.height = 80;
        layoutParams.width = 80;
        statusImg.setLayoutParams(layoutParams);
        statusImg.setImageResource(R.drawable.play);
        relativeLayout.setOnClickListener(new ChildItemOnClickListener(album, childPosition));
        return relativeLayout;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return  true;
    }

}
