package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/1/13.
 */
@Data
public class ApplicationSubmit {
    private int id;
    private String openid; // 提交人openid
    private Integer applicationId; // 应用练习Id
    private Integer planId; // 提升计划id
    private String content; // 提交内容
    private Integer pointStatus; //加分状态
    private Integer priority; // 排序优先级
    private Date highlightTime; //加精时间
    private Date updateTime; // 最后更新时间
    private Date publishTime; //发布时间
    private String upTime; //非db字段 提交时间
    private String headPic; //非db字段 提交人头像
    private String upName; //非db字段 提交人昵称
}
