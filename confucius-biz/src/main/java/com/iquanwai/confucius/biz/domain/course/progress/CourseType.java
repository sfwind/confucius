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

    public static final String PICURL_PREFIX = ConfigUtils.domainName()+"/images/";

    public static final Map<Integer, String> iconLockMap = Maps.newHashMap();
    public static final Map<Integer, String> iconUnLockMap = Maps.newHashMap();
    public static final Map<Integer, String> iconCompleteMap = Maps.newHashMap();

    static{
        iconLockMap.put(CHALLENGE, PICURL_PREFIX+"锁定图标.png");
        iconLockMap.put(HOMEWORK, PICURL_PREFIX+"锁定图标.png");
        iconLockMap.put(ASSESSMENT, PICURL_PREFIX+"群点评锁定图标.png");
        iconLockMap.put(RELAX, PICURL_PREFIX+"休息日图标.png");
        iconLockMap.put(GRADUATE, PICURL_PREFIX+"毕业锁定图标.png");

        iconUnLockMap.put(CHALLENGE, PICURL_PREFIX+"待完成图标.png");
        iconUnLockMap.put(HOMEWORK, PICURL_PREFIX+"待完成图标.png");
        iconUnLockMap.put(ASSESSMENT, PICURL_PREFIX+"群点评解锁图标.png");
        iconUnLockMap.put(RELAX, PICURL_PREFIX+"休息日图标.png");
        iconUnLockMap.put(GRADUATE, PICURL_PREFIX+"毕业解锁图标.png");

        iconCompleteMap.put(CHALLENGE, PICURL_PREFIX+"完成图标.png");
        iconCompleteMap.put(HOMEWORK, PICURL_PREFIX+"完成图标.png");
        iconCompleteMap.put(ASSESSMENT, PICURL_PREFIX+"群点评锁定图标.png");
        iconCompleteMap.put(RELAX, PICURL_PREFIX+"休息日图标.png");
        iconCompleteMap.put(GRADUATE, PICURL_PREFIX+"毕业锁定图标.png");
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
