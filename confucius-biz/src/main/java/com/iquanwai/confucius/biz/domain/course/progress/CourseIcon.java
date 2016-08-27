package com.iquanwai.confucius.biz.domain.course.progress;

/**
 * Created by justin on 16/8/25.
 */
public enum CourseIcon {
    CHALLENGE(1,""),HOMEWORK(2,""),ASSESSMENT(3,""),RELAX(4,"");

    private int courseType;
    private String icon;

    CourseIcon(int courseType, String iconUrl) {
        this.courseType = courseType;
        this.icon = iconUrl;
    }
}
