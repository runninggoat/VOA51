package com.dongbao.voa51.utils;

import android.util.Log;

import com.dongbao.voa51.MetaData;
import com.dongbao.voa51.models.Album;
import com.dongbao.voa51.models.News;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类，51voa.com的爬虫，提供了方法从目标路径爬取主页信息、新闻页信息的方法
 */
public class VOA51Crawler {

    private static final String TAG = "VOA51Crawler";
    private static final String[] ignoreArray = {
            "[ Bilingual News ]",
            "[ Everyday Grammar TV ]",
            "[ English @ the Movies ]",
            "[ English in a Minute ]",
            "[ Learning English Videos ]",
            "[ News Words ]"
    };

    private static final int TIMEOUT = 10000;

    /**
     * 爬取首页发内容并整理数据到一个map中
     * @param url 首页地址链接
     * @return 合集列表
     */
    public static List<Album> crawlIndexPage(String url) {
        if (MetaData.LOG_ON) Log.d(TAG, String.format("VOA51Crawler.crawlIndexPage\n%s", url));
        String baseURL = url;
        Document doc = null;
        List<Album> albumList = null;
        try {
            doc = Jsoup.connect(url).timeout(TIMEOUT).get();
            Elements newsElements = doc.select("#list ul li");
            albumList = new ArrayList<>();
            Map<String, Integer> indexMap = new HashMap<>();
            for (Element newsElement : newsElements) {
                String tag = newsElement.select("a").first().text();
                if (discard(tag)) {
                    continue;
                }
                Elements as = newsElement.select("a");
                for (Element a : as) {
                    if (!a.text().startsWith("[") && a.text().endsWith(")")) {
                        try {
                            String title = IllegalCharacterFilter.newsNameFilter(a.text());
                            String ref = baseURL + a.attr("href");
                            String dateString = extractDateString(title);
                            News news = new News(dateString);
                            news.setUrl(ref);
                            news.setTitle(extractNewsTitle(title));
                            if (indexMap.containsKey(dateString)) {
                                int index = indexMap.get(dateString);
                                albumList.get(index).addNews(news);
                            } else {
                                Album album = new Album(dateString);
                                album.addNews(news);
                                albumList.add(album);
                                indexMap.put(dateString, albumList.size() - 1);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
//                        System.out.println(a.text() + ": " + url + a.attr("href"));
                    }
                }
            }
//            System.out.println(dayAlbumList);
        } catch (SocketTimeoutException timeoutException) {
            timeoutException.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return albumList;
    }

    /**
     * 爬取新闻页的内容并整理成一条新闻信息返回
     * @param news 新闻信息，未得到新闻页的信息
     * @return 返回新闻信息实例，包含爬取的新闻页信息
     */
    public static News crawlNewsPage(News news) {
        if (MetaData.LOG_ON) Log.d(TAG, String.format("VOA51Crawler.crawlNewsPage\n%s", news.getUrl()));
        Document doc = null;
        String mp3DownloadLink = null;
        try {
            doc = Jsoup.connect(news.getUrl()).get();
            Elements mp3Links = doc.select("#mp3");
            for (Element mp3Link : mp3Links) {
                mp3DownloadLink = mp3Link.attr("href");
                if (mp3DownloadLink.startsWith("http://"))
                    break;
            }
            news.setAudioUrl(mp3DownloadLink);
            Elements paragraphElements = doc.select("#content p");
            for (Element paragraphElement : paragraphElements) {
                news.getParagraphs().add(paragraphElement.text());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return news;
        }
    }

    /**
     * 下载新闻音频
     * @param url 新闻音频地址链接
     * @param dateString 日期字符串，用于确定保存音频文件的路径
     * @param fileName 音频文件名字，一般为新闻标题，从首页抓取并去除了不适用于文件名的特殊字符
     * @return 返回保存的音频文件的文件类实例
     */
    public static File downloadMp3(String url, String dateString, String fileName) {
        if (MetaData.LOG_ON) Log.d(TAG, String.format("VOA51Crawler.downloadMp3\nurl:%s\ndate:%s\nfile:%s", url, dateString, fileName));
        String savePath = Publics.self.getFilesDir() + File.separator + dateString + File.separator;
        return HttpDownloader.downloadFile(url, savePath, fileName);
    }

    private static boolean discard(String tag) {
        for (String t : ignoreArray) {
            if (t.equals(tag)) {
                return true;
            }
        }
        return false;
    }

    public static String extractDateString(String title) throws Exception {
        String dateRegex = "\\d{4}-\\d+-\\d+";
        Pattern p = Pattern.compile(dateRegex);
        Matcher matcher = p.matcher(title);
        if (!matcher.find()) {
            throw new Exception(title + "题目中找不到日期");
        }
//        System.out.println("group count: "+matcher.groupCount());
        return matcher.group();
    }

    public static String extractNewsTitle(String title) throws Exception {
        String dateRegex = ".* \\(";
        Pattern p = Pattern.compile(dateRegex);
        Matcher matcher = p.matcher(title);
        if (!matcher.find()) {
            throw new Exception(title + "题目中找不到新闻题目");
        }
        String matchedString = matcher.group();
        return matchedString.substring(0, matchedString.length()-2);
    }

}
