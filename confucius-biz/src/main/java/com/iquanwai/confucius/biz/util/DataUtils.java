package com.iquanwai.confucius.biz.util;

import java.util.regex.Pattern;

public class DataUtils {

    public static final Pattern integerPattern = Pattern.compile("^[-\\+]?[\\d]*$");

    /**
     * 判断str是否是数字类型
     * @param str
     * @return
     */
    public static boolean isInteger(String str){
        return integerPattern.matcher(str).matches();
    }
}
