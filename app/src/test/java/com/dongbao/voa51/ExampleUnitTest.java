package com.dongbao.voa51;

import com.dongbao.voa51.models.Album;
import com.dongbao.voa51.models.News;
import com.dongbao.voa51.utils.FileManager;
import com.dongbao.voa51.utils.VOA51Crawler;

import org.junit.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the
 * development machine (host).
 *
 * @see
 * <a href="http://d.android.com/tools/testing">Testing
 * documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testCrawlIndexPage() {
        String url = "http://www.51voa.com/";
        List<Album> albumList = VOA51Crawler.crawlIndexPage(url);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-M-d");
        Date date = new Date();
        for (Album album : albumList) {
            System.out.println(album.getDateString());
        }
        assertTrue(albumList.get(0).getDateString().equals(simpleDateFormat.format(date)));
    }

    @Test
    public void testCrawlNewsPage() {
        String url = "http://www.51voa.com/VOA_Special_English/celebrating-an-italian-easter-in-washington78500.html";
        String dateString = "2018-4-1";
        String title = "Celebrating an Italian Easter in Washington";
        News news = new News(dateString);
        news.setUrl(url);
        news.setTitle(title);
        news = VOA51Crawler.crawlNewsPage(news);
        System.out.println(news);
        assertEquals(dateString, news.getDateString());
        assertEquals(title, news.getTitle());
        assertEquals(url, news.getUrl());
        assertEquals("http://downdb.51voa.com/201803/celebrating-an-italian-easter-in-washington.mp3", news.getAudioUrl());
    }

    @Test
    public void testDownloadMp3() {
        MetaData.SHOW_NOTI = false;
        String url = "http://downdb.51voa.com/201803/celebrating-an-italian-easter-in-washington.mp3";
        String dateString = "2018-4-1";
        String fileName = "Celebrating an Italian Easter in Washington.mp3";
        File savedFile = VOA51Crawler.downloadMp3(url, dateString, fileName);
        System.out.println(savedFile.getAbsolutePath());
        assertTrue(savedFile.exists());
//        try {
//            Thread.sleep(30000L);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        FileManager.clearFile(savedFile);
//        FileManager.clearFile(savedFile.getParentFile());
    }
}