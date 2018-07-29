package com.dongbao.voa51.models;

import com.dongbao.voa51.utils.Publics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 新闻集合类，保存了某个日期的所有新闻
 */
public class Album implements Serializable {

    private String dateString;
    private List<News> newsList;

    public Album(String dateString) {
        this.dateString = dateString;
        this.newsList = new ArrayList<>();
    }

    public Album(String dateString, List<News> newsList) {
        this.dateString = dateString;
        this.newsList = newsList;
    }

    public void addNews(News news) {
        this.newsList.add(news);
    }

    public List<News> getNewsList() {
        return newsList;
    }

    public int getAlbumSize() {
        return newsList.size();
    }

    public News getNewsAudioAtPosition(int position) {
        return newsList.get(position);
    }

    public String getDateString() {
        return dateString;
    }

    @Override
    public String toString() {
        return Publics.gson.toJson(this);
    }
}
