package com.iquanwai.confucius.biz.po.asst;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 助教升级默认标准
 */
@Data
public class AsstUpDefault {

//    CREATE TABLE quanwai.AsstUpDefault (
//    LearingProblem int(11) DEFAULT 0  COMMENT '累计学习小课数量',
//    ReviewNumber int(11) DEFAULT 0  COMMENT '最低需要满足的点评数',
//    ValidReviewRate TINYINT(4) DEFAULT NULL COMMENT '有效的点评所占的比例（百分之）',
//    HighQualityAnswer int(11) DEFAULT 0 COMMENT '优质回答数',
//    HostNumber int(11) DEFAULT 0 COMMENT '主持人次数',
//    HostScore DECIMAL(5,2) DEFAULT 0  COMMENT '主持人评分',
//    MainPointNumber int(11) DEFAULT 0 COMMENT '串讲人次数',
//    MainPointScore DECIMAL(5,2) DEFAULT 0 COMMENT '串讲人分数',
//    OnlineAnswer VARCHAR(5) DEFAULT 'N' COMMENT '线上答题演习',
//    Swing VARCHAR(5) DEFAULT 'N' COMMENT '吊打演习',
//    OnlineOrSwingNumber int(11) DEFAULT 0 COMMENT '线上答疑或吊打次数',
//    OnlineScore DECIMAL(5,2) DEFAULT 0 COMMENT '线上活动反馈分数',
//    CampNumber int(11) DEFAULT 0 COMMENT '训练营次数',
//    AsstNumber INT(11) DEFAULT 0 COMMENT '大教练次数',
//    CampScore DECIMAL(5,2) DEFAULT 0 COMMENT  '训练营评分',
//    MonthlyWork VARCHAR(5) DEFAULT 'N' COMMENT '每月作业',
//    FosterNew  INT(11) DEFAULT 0 COMMENT '培养新人次数',
//    CompanyTrainNumber INT(11) DEFAULT 0 COMMENT '企业培训次数',
//    CompanyTrainScore DECIMAL(5,2) DEFAULT 0 COMMENT '企业培训评分',
//    Del tinyint(4) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
//    AddTime timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
//    UpdateTime timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
//    PRIMARY KEY (`Id`)
//)COMMENT='助教升级默认标准表'

    private Integer id;
    /**
     * 助教级别
     */
    private Integer roleId;
    
    /**
    * 倒计时
    */
    private Integer  countDown;
    
    /**
    * 累计学习小课数量
    */
    private Integer learningProblem;
    
    /**
    * 最低需要满足的点评数
    */
    private Integer reviewNumber;
    

    /**
    * 有效的点评所占的比例（百分之）
    */
    private Integer validReviewRate;
    

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
    private BigDecimal MainPointScore;
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
    private BigDecimal onlineScore ;
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
     * 是否删除 0-未删除 1-已删除
     */
    private Integer del;
}
