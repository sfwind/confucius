package com.iquanwai.confucius.biz.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    private static DateTimeFormatter format1 = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static DateTimeFormatter format2 = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static DateTimeFormatter format3 = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    private static DateTimeFormatter format4 = DateTimeFormat.forPattern("yyyy.MM.dd");
    private static DateTimeFormatter format5 = DateTimeFormat.forPattern("yyyy年MM月dd日");
    private static DateTimeFormatter format6 = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    private static DateTimeFormatter timeFormat = DateTimeFormat.forPattern("HH:mm");


    public static String parseDateToTimeFormat(Date date) {
        return timeFormat.print(new DateTime(date));
    }

    public static String parseDateToFormat5(Date date) {
        return format5.print(new DateTime(date));
    }

    public static String parseDateToFormat6(Date date) {
        return format6.print(new DateTime(date));
    }

    public static String parseDateToString(Date date) {
        return format1.print(new DateTime(date));
    }

    public static String parseDateToStringByCommon(Date date) {
        return format4.print(new DateTime(date));
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
        long now = System.currentTimeMillis();
        long thatTime = date.getTime();

        return (int) Math.abs((now - thatTime) / 1000) / 60 / 60 / 24;
    }

    public static long nextDayRemainSeconds(Date tody) {
        Long current = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtils.afterDays(tody, 1));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date zero = calendar.getTime();
        long zeroTime = zero.getTime();
        return (zeroTime - current) / 1000;
    }

    public static long currentTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    public static String parseDateToString3(Date date) {
        return format3.print(new DateTime(date));
    }

    public static Date parseStringToDate3(String strDate) {
        return format3.parseDateTime(strDate).toDate();
    }

    public static Date afterMinutes(Date date, int increment) {
        return new DateTime(date).plusMinutes(increment).toDate();
    }

    public static Date afterHours(Date date, int increment) {
        return new DateTime(date).plusHours(increment).toDate();
    }

    /**
     * 自然月增加
     * @param date 某年某月某日
     * @param increment 增长多少个月
     */
    public static Date afterNatureMonths(Date date, int increment) {
//        // 获取dateTime
//        DateTime dateTime = new DateTime(date.getTime());
//        // 月内第几天
//        int monthDay = dateTime.getDayOfMonth();
//        // 周内第几天
//        int weekDay = dateTime.getDayOfWeek();
//        // 如果周内天数大于等于月内天数，则是第一个周日
//        if (weekDay >= monthDay) {
//            // 从这一天开始增加
//            return DateUtils.afterMonths(date, increment);
//        } else {
//            // 超过了第一个周日，算下个月第一天开始
//            Date firstDay = new DateTime(DateUtils.afterMonths(date, 1).getTime()).withDayOfMonth(1).toDate();
//            // 从下个月的第一天开始增加
//            return DateUtils.afterMonths(firstDay, increment);
//        }
        return DateUtils.afterMonths(date, increment);
    }

    public static Date startDay(Date date) {
        return new DateTime(date).withTimeAtStartOfDay().toDate();
    }

    public static Date afterYears(Date date, int increment) {
        return new DateTime(date).plusYears(increment).toDate();
    }

    public static Date afterMonths(Date date, int increment) {
        return new DateTime(date).plusMonths(increment).toDate();
    }

    public static Date afterDays(Date date, int increment) {
        return new DateTime(date).plusDays(increment).toDate();
    }

    public static Date beforeDays(Date date, int increment) {
        return new DateTime(date).minusDays(increment).toDate();
    }

    public static boolean isSameDate(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        boolean isSameYear = cal1.get(Calendar.YEAR) == cal2
                .get(Calendar.YEAR);
        boolean isSameMonth = isSameYear
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
        boolean isSameDate = isSameMonth
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2
                .get(Calendar.DAY_OF_MONTH);

        return isSameDate;
    }

    public static boolean isToday(Date date) {
        String cmpDate = date.toString().substring(0, 10);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date()).substring(0, 10);
        return today.equals(cmpDate);
    }

    public static Integer getYear(Date date) {
        return new DateTime(date).getYear();
    }

    public static Integer getMonth(Date date) {
        return new DateTime(date).getMonthOfYear();
    }

    public static Date endDateOfMonth(Integer month) {
        DateTime date = new DateTime().monthOfYear().setCopy(month).dayOfMonth().withMaximumValue();
        return date.toDate();
    }

}
