package com.iquanwai.confucius.biz.domain.course.progress;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.util.ConfigUtils;

import java.util.Map;

/**
 * Created by justin on 16/8/25.
 */
public class CourseType {
    public static final int CHALLENGE = 1;
    public static final int HOMEWORK = 2;
    public static final int ASSESSMENT = 3;
    public static final int RELAX = 4;
    public static final int GRADUATE =5;
    public static final int NEW_CHALLENGE =6;
    public static final int NEW_HOMEWORK =7;

    public static final String PICURL_PREFIX = ConfigUtils.resourceDomainName()+"/images/";

    public static final Map<Integer, String> iconLockMap = Maps.newHashMap();
    public static final Map<Integer, String> iconUnLockMap = Maps.newHashMap();
    public static final Map<Integer, String> iconCompleteMap = Maps.newHashMap();

    static{
        iconLockMap.put(CHALLENGE, PICURL_PREFIX+"course_locked.png");
        iconLockMap.put(HOMEWORK, PICURL_PREFIX+"course_locked.png");
        iconLockMap.put(ASSESSMENT, PICURL_PREFIX+"group_talk_locked.png");
        iconLockMap.put(RELAX, PICURL_PREFIX+"relax_day.png");
        iconLockMap.put(GRADUATE, PICURL_PREFIX+"graduate_locked.png");
        iconLockMap.put(NEW_CHALLENGE, PICURL_PREFIX+"course_locked.png");
        iconLockMap.put(NEW_HOMEWORK, PICURL_PREFIX+"course_locked.png");

        iconUnLockMap.put(CHALLENGE, PICURL_PREFIX+"course_incomplete.png");
        iconUnLockMap.put(HOMEWORK, PICURL_PREFIX+"course_incomplete.png");
        iconUnLockMap.put(ASSESSMENT, PICURL_PREFIX+"group_talk_unlock.png");
        iconUnLockMap.put(RELAX, PICURL_PREFIX+"relax_day.png");
        iconUnLockMap.put(GRADUATE, PICURL_PREFIX+"graduate_unlock.png");
        iconUnLockMap.put(NEW_CHALLENGE, PICURL_PREFIX+"course_incomplete.png");
        iconUnLockMap.put(NEW_HOMEWORK, PICURL_PREFIX+"course_incomplete.png");

        iconCompleteMap.put(CHALLENGE, PICURL_PREFIX+"course_complete.png");
        iconCompleteMap.put(HOMEWORK, PICURL_PREFIX+"course_complete.png");
        iconCompleteMap.put(ASSESSMENT, PICURL_PREFIX+"group_talk_locked.png");
        iconCompleteMap.put(RELAX, PICURL_PREFIX+"relax_day.png");
        iconCompleteMap.put(GRADUATE, PICURL_PREFIX+"graduate_locked.png");
        iconCompleteMap.put(NEW_CHALLENGE, PICURL_PREFIX+"course_complete.png");
        iconCompleteMap.put(NEW_HOMEWORK, PICURL_PREFIX+"course_complete.png");
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
