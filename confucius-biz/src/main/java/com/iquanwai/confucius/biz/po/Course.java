package com.iquanwai.confucius.biz.po;

import lombok.Data;

import java.util.List;

/**
 * Created by justin on 16/8/25.
 */
@Data
public class Course {
    private int id;
    private String name;  //课程名称
    private Integer length; //开课天数
    private Integer week; //开课周数
    private String pic;   //课程图片url
    private boolean preChapter;   //是否存在课程准备（0-无，1-有）
    private String certificatePic;   //证书背景图片
    private Integer type; //课程类型（1-长课程，2-短课程）
    private List<Chapter> chapterList;
}
