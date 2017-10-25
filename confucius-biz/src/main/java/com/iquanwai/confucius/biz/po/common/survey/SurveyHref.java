package com.iquanwai.confucius.biz.po.common.survey;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/9/4.
 */
@Data
public class SurveyHref {
    private Integer id;
    private Integer activity;
    private String name;
    private String realHref; // 实际的问卷星链接
    private Boolean del;
    private Date addTime;

    // 非DB字段
    private String mobileHref; // 移动端访问链接
    private String pcHref; // pc段访问链接

}
