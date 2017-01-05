package com.iquanwai.confucius.biz.po;

import lombok.Data;

import java.util.Date;

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
    private Integer type; //课程类型（1-长课程，2-短课程）
    private String intro; //文字介绍
    private String introPic; //介绍课程的图片url
    private Boolean hidden; //是否在报名页隐藏（0-否，1-是）
    private Date updateTime;
}
