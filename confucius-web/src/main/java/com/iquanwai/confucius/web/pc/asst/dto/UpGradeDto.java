package com.iquanwai.confucius.web.pc.asst.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 返回给前端的信息
 */
@Data
public class UpGradeDto {

    /**
     * 剩余天数
     */
    private Integer remainDay;

    /**
     * 已经学习课程数量
     */
    private Integer learnedProblem;

    /**
     * 未完成的课程数量
     */
    private Integer remainProblem;

    /**
     * 已经完成的点评数
     */
    private Integer reviewedNumber;

    /**
     * 还需要的点评数
     */
    private Integer remainReviewNumber;

    /**
     * 有效点评率
     */
    private Integer reviewRate;
    /**
     * 已经完成的优质回答数
     */
    private Integer highAnswer;
    /**
     * 剩余需要的优质回答数
     */
    private Integer remainHighAnswer;

    /**
     * 主持次数
     */
    private Integer hostNumber;
    /**
     * 还需主持次数
     */
    private Integer remainHostNumber;
    /**
     * 主持评分
     */
    private BigDecimal hostScore;
    /**
     * 串讲人次数
     */
    private Integer mainPointNumber;
    /**
     * 还需串讲人次数
     */
    private Integer remainPointNumber;
    /**
     * 串讲人评分
     */
    private BigDecimal mainPointScore;
    /**
     * 线上答疑
     */
    private String onlineAnswer;
    /**
     * 吊打演习
     */
    private String Swing;

    /**
     * 参加线上答疑或吊打演习次数
     */
    private Integer onlineOrSwingNumber;

    /**
     * 剩余线上答题或吊打演习次数
     */
    private Integer remainOnlineOrSwingNumber;
    /**
     * 线上活动评分
     */
    private BigDecimal onlineScore;
    /**
     * 训练营完成次数
     */
    private Integer campNumber;
    /**
     * 还需训练营完成次数
     */
    private Integer remainCampNumber;
    /**
     * 大教练次数
     */
    private Integer AsstNumber;
    /**
     * 剩余大教练次数
     */
    private Integer remainAsstNumber;
    /**
     * 训练营评分
     */
    private BigDecimal campScore;
    /**
     * 每月作业
     */
    private String monthlyWork;
    /**
     * 培养新人次数
     */
    private Integer fosterNew;
}
