package com.iquanwai.confucius.biz.po.asst;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 助教升级实际情况
 */
@Data
public class AsstUpExecution {

    private Integer id;

    private Integer profileId;
    /**
     * 助教级别
     */
    private Integer roleId;
    /**
     * 开始时间
     */
    private Date startDate;
    /**
     * 点评数
     */
    private Integer reviewNumber;
    /**
     * 求点评的回答数
     */
    private Integer requestReviewNumber;

    /**
     * 有效的点评量
     */
    private Integer validReviewNumber;
    /**
     * 优质回答数
     */
    private Integer highQualityAnswer;
    /**
     * 主持人次数
     */
    private Integer hostNumber;
    /**
     * 主持人评分
     */
    private BigDecimal hostScore;
    /**
     * 串讲人次数
      */
    private Integer mainPointNumber;
    /**
     * 串讲人分数
     */
    private BigDecimal mainPointScore;
    /**
     * 线上答题演习
     */
    private String onlineAnswer;
    /**
     * 吊打演习
     */
    private String swing;
    /**
     * 线上答题或吊打次数
     */
    private Integer onlineOrSwingNumber;
    /**
     * 线上活动反馈分数
     */
    private BigDecimal onlineScore;
    /**
     * 训练营次数
     */
    private Integer campNumber;

    /**
     * 大教练次数
     */
    private Integer asstNumber;
    /**
     * 训练营评分
     */
    private BigDecimal campScore;
    /**
     * 每月作业
     */
    private String monthlyWork;

    /**
     * 缺少作业数
     */
    private Integer lackTask;

    /**
     * 培养新人次数
     */
    private Integer fosterNew;
    /**
     * 企业培训次数
     */
    private Integer companyTrainNumber;
    /**
     * 企业培训评分
     */
    private BigDecimal companyTrainScore;

    /**
     * 认证状态
     */
    private String upGrade;
    /**
     * 是否删除 0-未删除 1-已删除
     */
    private Integer del;



}
