package com.iquanwai.confucius.biz.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class DateUtils {
    private static DateTimeFormatter format1 = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static DateTimeFormatter format2 = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    public static String parseDateToString(Date date) {
        return format1.print(new DateTime(date));
    }

    public static Date parseStringToDate(String strDate) {
        return format1.parseDateTime(strDate).toDate();
    }

    public static String parseDateTimeToString(Date date) {
        return format2.print(new DateTime(date));
    }

    public static Date parseStringToDateTime(String strDate) {
        return format2.parseDateTime(strDate).toDate();
    }

    public static int interval(Date date) {
        long now = new Date().getTime();
        long thatTime = date.getTime();

        return Math.abs((int)(now - thatTime)/1000)/60/60/24;
    }

    public static long currentTimestamp(){
        return System.currentTimeMillis()/1000;
    }

}
