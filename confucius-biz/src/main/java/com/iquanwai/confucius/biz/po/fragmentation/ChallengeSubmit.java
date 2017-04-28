package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class ChallengeSubmit {
    private int id;
    private String openid; // 提交人openid 
    private Integer challengeId; // 小目标id
    private Integer planId; // 提升计划id 
    private String content; // 提交内容
    private Integer length; //字数
    private Integer pointStatus;
    private Date updateTime;
}
