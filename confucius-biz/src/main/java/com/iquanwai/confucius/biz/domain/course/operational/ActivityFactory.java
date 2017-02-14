package com.iquanwai.confucius.biz.domain.course.operational;

import com.iquanwai.confucius.biz.util.DateUtils;

/**
 * Created by justin on 17/2/14.
 */
public class ActivityFactory {
    public static final String ACTIVITY_CAREER_COURSE_PACKAGE = "CAREER_COURSE_PACKAGE";

    public static Activity getActivity(String name){
        if(name!=null && name.equals(ACTIVITY_CAREER_COURSE_PACKAGE)){
            //职业课程推广活动
            return new Activity().discount(9.0).name(name).promoCodeUsageLimit(5)
                    .endDate(DateUtils.parseStringToDate("2017-03-31"));
        }

        return null;
    }
}
