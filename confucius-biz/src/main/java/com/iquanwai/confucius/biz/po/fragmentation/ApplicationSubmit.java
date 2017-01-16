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
    private Integer applicationId; // 应用训练Id
    private Integer planId; // 提升计划id
    private String content; // 提交内容
    private Integer pointStatus;
    private Date updateTime;
}
