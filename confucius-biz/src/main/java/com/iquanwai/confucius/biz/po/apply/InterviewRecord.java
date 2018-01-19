package com.iquanwai.confucius.biz.po.apply;

import lombok.Data;

@Data
public class InterviewRecord {

    private Integer id;
    private Integer profileId;
    private Integer interviewerId;
    /**
     * 审核人id
     */
    private Integer approvalId;
    /**
     * 申请id号
     */
    private Integer applyId;
    /**
     * 面试时间
     */
    private String interviewTime;
    /**
     * 学员问题
     */
    private String question;
    /**
     * 关注渠道
     */
    private String focusChannel;

    /**
     * 选择其他后填写的关注渠道内容
     */
    private String focusChannelName;

    /**
     * 接触时长
     */
    private String touchDuration;

    /**
     * 选择其他后填写的接触时长内容
     */
    private String touchDurationName;
    /**
     * 触发申请的事件
     */
    private String applyEvent;

    /**
     * 选择其他后填写的触发申请事件的内容
     */
    private String applyEventName;
    /**
     * 学习意愿评估
     */
    private Integer learningWill;

    /**
     * 发展潜力评估
     */
    private Integer potentialScore;

    /**
     * 是否申请奖学金
     */
    private Integer applyAward;
    /**
     * 申请理由
     */
    private String applyReason;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 是否删除
     */
    private Integer del;
}


