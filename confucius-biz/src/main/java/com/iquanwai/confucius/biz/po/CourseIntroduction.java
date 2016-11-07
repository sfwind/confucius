package com.iquanwai.confucius.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/9/29.
 */
@Data
public class CourseIntroduction {
    private int id;
    private Integer courseId; //课程id
    private String courseName;  //课程名称
    private Boolean free; //是否免费
    private Double fee;   //课程费用
    private Integer length; //开课天数
    private Integer taskLength; //有任务的天数
    private Integer week; //开课周数
    private String voice; //语音介绍
    private String intro; //文字介绍
    private String introPic; //介绍课程的图片url
}
