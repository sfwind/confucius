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
    private Integer challengeId; // 挑战训练id 
    private Integer planId; // 提升计划id 
    private String content; // 提交内容
    private Integer pointStatus;
    private String submitUrl;// 提交url 
    private String shortUrl;// 短链接
    private Date UpdateTime;
}
