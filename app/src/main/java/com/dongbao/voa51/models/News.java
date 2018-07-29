package com.dongbao.voa51.models;

import com.dongbao.voa51.utils.Publics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 新闻实体类，保存了新闻的标题、新闻内容、音频文件路径等信息，对应数据表news
 */
public class News implements Serializable {

    private int news_id;
    private String dateString; //新闻的日期
    private String url; //新闻页地址链接
    private String audioUrl; //新闻音频地址链接
    private String localMp3Uri; //本地音频URI路径
    private String title; //新闻标题
    private List<String> paragraphs; //新闻各个段落

    public News(String dateString) {
        this.dateString = dateString;
        paragraphs = new ArrayList<>();
    }

    public int getNews_id() {
        return news_id;
    }

    public void setNews_id(int news_id) {
        this.news_id = news_id;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getDateString() {
        return dateString;
    }

    public String getUrl() {
        return url;
    }

    public String getLocalMp3Uri() {
        return localMp3Uri;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setLocalMp3Uri(String localMp3Path) {
        this.localMp3Uri = localMp3Path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getParagraphs() {
        return paragraphs;
    }

    public String getContent() {
        StringBuilder sb = new StringBuilder();
        for (String s : paragraphs) {
            sb.append(s);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return Publics.gson.toJson(this);
    }
}
