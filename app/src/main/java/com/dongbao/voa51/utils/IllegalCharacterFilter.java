package com.dongbao.voa51.utils;

import java.util.regex.Pattern;

/**
 * Created by 15018 on 2017/5/26.
 */

public class IllegalCharacterFilter {
    private static Pattern FilePattern = Pattern.compile("[\\\\/:*?\"<>|]");

    public static String newsNameFilter(String str) {
        if (str == null) {
            return null;
        }
        str = FilePattern.matcher(str).replaceAll("");
//        str = str.replaceAll("\\s", "_");
        str = str.replaceAll("‘", "");
        str = str.replaceAll("’", "");
        return str;
    }
}
