package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

import java.util.Date;

/**
 * Created by 三十文
 */
@Data
public class KnowledgeDiscuss {

    private Integer id;
    // 知识点 id
    private Integer knowledgeId;
    // 评论内容
    private String comment;
    // 评论人 openid
    private String openId;
    // 评论人 profileid
    private Integer profileId;
    // 优先级
    private Boolean priority;
    // 被回复的评论 id
    private Integer repliedId;
    // 被回复的人 openid
    private String repliedOpenid;
    // 被回复的人 profileid
    private Integer repliedProfileId;
    // 被回复的评论内容
    private String repliedComment;
    // 添加
    private Date addTime;

}
