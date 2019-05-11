package com.tsitsing.translation.utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyDateUtil {
    //返回粗略时间
    public static String getSketchyDate () {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        return dateFormat.format(new Date());
    }
    //返回详细时间
    public static String getDetailedDate () {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return dateFormat.format(new Date());
    }
}
