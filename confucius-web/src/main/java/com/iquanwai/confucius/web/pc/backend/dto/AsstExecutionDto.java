package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AsstExecutionDto {

    private Integer id;

    private String nickName;

    private String roleName;
    /**
     * 考核开始日期
     */
    private Date startDate;

    private Integer countDown;

    private Integer learnedProblem;

    /**
     * 点评数
     */
    private Integer reviewNumber;

    /**
     * 求点评的回答数
     */
    private Integer requestReviewNumber;

    /**
     * 有效的点评数
     */
    private Integer validReviewNumber;
    /**
     * 有效点评率
     */
    private Integer ValidReviewRate;
    /**
     * 优质回答数
     */
    private Integer highQualityAnswer;

    /**
     * 主持次数
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
     * 串讲人评分
     */
    private BigDecimal mainPointScore;

    private String onlineAnswer;

    private String swing;

    private Integer onlineOrSwingNumber;

    private BigDecimal onlineScore;

    private Integer campNumber;

    private Integer asstNumber;

    private BigDecimal campScore;

    private String monthlyWork;

    private Integer fosterNew;

    private Integer companyTrainNumber;

    private BigDecimal companyTrainScore;
}
