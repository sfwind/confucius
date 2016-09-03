package com.iquanwai.confucius.biz.dao.po;

import lombok.Data;

import java.util.List;

/**
 * Created by justin on 16/8/25.
 */
@Data
public class Course {
    private int id;
    private Integer type; //课程类型
    private String name;  //课程名称
    private Integer difficulty; //难易度（简单-1，普通-2，困难-3）
    private Boolean free; //是否免费
    private Double fee;   //课程费用
    private Integer length; //开课天数
    private Integer week; //开课周数
    private String pic;   //课程图片url
    private String introPic; //介绍课程的图片url
    private List<Chapter> chapterList;
}
