package com.iquanwai.confucius.biz.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class DateUtils {
    private static DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    public static String parseDateToString(Date date) {
        return format.print(new DateTime(date));
    }

    public static Date parseStringToDate(String strDate) {
        return format.parseDateTime(strDate).toDate();
    }

    public static int getMinuteByDate(Date date) {
        if (date == null) {
            return 0;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.getMinuteOfHour();
    }

    public static Date minuteAfter(Date date, int minute){
        DateTime dateTime = new DateTime(date);
        return dateTime.plusMinutes(minute).toDate();
    }

    public static long currentTimestamp(){
        return System.currentTimeMillis()/1000;
    }

}
