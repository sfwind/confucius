package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class GradeDto {
    private Integer id;

    private String nickName;

    private String roleName;
    /**
     *开始日期
     */
    private Date startDate;

    /**
     * 倒计时天数
     */
    private Integer countDown;

    /**
     * 剩余天数
     */
    private Integer remainDay;

    /**
     * 需要完成的课程数量
     */
    private Integer needLearnedProblem;
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
     * 需要完成的求点评数
     */
    private Integer needRequestReviewNumber;

    /**
     * 已经完成的求点评数
     */
    private Integer requestReviewNumber;
    /**
     * 剩余的求点评数
     */
    private Integer remainRequestReviewNumber;


    /**
     * 需要完成的有效点评数
     */
    private Integer needValidReviewNumber;

    /**
     * 完成的有效点评数
     */
    private Integer validReviewNumber;


    /**
     * 需要的点评率
     */
    private Integer needReviewRate;

    /**
     * 有效点评率
     */
    private Integer reviewRate;
    /**
     * 需要完成的优质回答数
     */
    private Integer needHighAnswer;

    /**
     * 已经完成的优质回答数
     */
    private Integer highAnswer;
    /**
     * 剩余需要的优质回答数
     */
    private Integer remainHighAnswer;

    /**
     * 需要的主持次数
     */
    private Integer needHostNumber;
    /**
     * 已经完成的主持次数
     */
    private Integer hostNumber;
    /**
     * 还需主持次数
     */
    private Integer remainHostNumber;
    /**
     * 需要的主持评分
     */
    private BigDecimal needHostScore;
    /**
     * 主持评分
     */
    private BigDecimal hostScore;

    /**
     * 需要的串讲次数
     */
    private Integer needMainPointNumber;

    /**
     * 串讲人次数
     */
    private Integer mainPointNumber;

    /**
     * 还需串讲人次数
     */
    private Integer remainPointNumber;

    private BigDecimal needPointScore;
    /**
     * 串讲人评分
     */
    private BigDecimal mainPointScore;

    private String needOnlineAnswer;
    /**
     * 线上答疑
     */
    private String onlineAnswer;

    private String needSwing;
    /**
     * 吊打演习
     */
    private String Swing;

    /**
     * 需要线上答疑或吊打的次数
     */
    private Integer needOnlineNumber;

    /**
     * 参加线上答疑或吊打演习次数
     */
    private Integer onlineOrSwingNumber;

    /**
     * 剩余线上答题或吊打演习次数
     */
    private Integer remainOnlineOrSwingNumber;
    /**
     * 需要的线上评分
     */
    private BigDecimal needOnlineScore;

    /**
     * 线上活动评分
     */
    private BigDecimal onlineScore;

    /**
     * 需要完成次数
     */
    private Integer needCampNumber;

    /**
     * 训练营完成次数
     */
    private Integer campNumber;
    /**
     * 还需训练营完成次数
     */
    private Integer remainCampNumber;

    /**
     * 需要大教练的次数
     */
    private Integer needAsstNumber;

    /**
     * 大教练次数
     */
    private Integer asstNumber;
    /**
     * 剩余大教练次数
     */
    private Integer remainAsstNumber;
    /**
     * 需要训练营的评分
     */
    private BigDecimal needCampScore;
    /**
     * 训练营评分
     */
    private BigDecimal campScore;

    private String needMonthlyWork;
    /**
     * 每月作业
     */
    private String monthlyWork;
    /**
     * 需要培养新人的次数
     */
    private Integer needFosterNew;
    /**
     * 培养新人次数
     */
    private Integer fosterNew;
    /**
     * 还需培养新人的次数
     */
    private Integer remainFosterNew;

    /**
     * 需要企业培训的次数
     */
    private Integer needCompanyNumber;
    /**
     * 企业培训次数
     */
    private Integer companyNumber;
    /**
     * 剩余企业培训次数
     */
    private Integer remainCompanyNumber;

    /**
     * 需要企业培训的分数
     */
    private BigDecimal needCompanyScore;
    /**
     * 企业培训的分数
     */
    private BigDecimal companyTrainScore;
    /**
     * 是否需要认证
     */
    private String needVerified;

    /**
     * 升级状态
     */
    private String upGrade;

}
