package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/1/13.
 */
@Data
public class ApplicationSubmit {
    private int id;
    private String openid; //提交用户openid
    private Integer profileId; //用户id
    private Integer applicationId; //应用练习id
    private Integer planId; //提升计划id
    private String content; //提交内容
    private Integer pointStatus; //是否已加分（0-否，1-是）
    private Integer priority; // 排序优先级
    private Date updateTime; //最后更新时间
    private Date highlightTime; //加精时间
    private Date publishTime; // 第一次提交时间
    private Date lastModifiedTime; //最近一次内容提交时间
    private Boolean requestFeedback; //是否求点评
    private Boolean feedback; // 教练是否已点评
    private Integer length; //字数
    private Integer problemId;//小课id

    private String upTime; //非db字段 提交时间
    private String headPic; //非db字段 提交人头像
    private String upName; //非db字段 提交人昵称
    private int comment; //非db字段 是否已被当前用户评论
    private String topic; //非db字段 应用练习标题
    private String description; //非db字段 应用练习说明
    private Integer del; //是否删除（0-否，1-是）
}
