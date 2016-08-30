package com.iquanwai.confucius.biz.domain.course.progress;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by justin on 16/8/25.
 */
public class CourseType {
    public static final int CHALLENGE = 1;
    public static final int HOMEWORK = 2;
    public static final int ASSESSMENT = 3;
    public static final int RELAX = 4;


    public static final Map<Integer, String> iconLockMap = Maps.newHashMap();
    public static final Map<Integer, String> iconUnLockMap = Maps.newHashMap();
    public static final Map<Integer, String> iconCompleteMap = Maps.newHashMap();

    static{
        iconLockMap.put(CHALLENGE, "");
        iconLockMap.put(HOMEWORK, "");
        iconLockMap.put(ASSESSMENT, "");
        iconLockMap.put(RELAX, "");

        iconUnLockMap.put(CHALLENGE, "");
        iconUnLockMap.put(HOMEWORK, "");
        iconUnLockMap.put(ASSESSMENT, "");
        iconUnLockMap.put(RELAX, "");

        iconCompleteMap.put(CHALLENGE, "");
        iconCompleteMap.put(HOMEWORK, "");
        iconCompleteMap.put(ASSESSMENT, "");
        iconCompleteMap.put(RELAX, "");
    }

    public static String getUrl(int type, boolean unlock, boolean complete){
        if(complete){
            return iconCompleteMap.get(type);
        }
        if(unlock){
            return iconUnLockMap.get(type);
        }

        return iconLockMap.get(type);
    }
}
