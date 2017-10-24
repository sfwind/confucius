package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

import java.util.Date;

@Data
public class MonthlyCampConfig {

    private Integer id;
    /**
     * 小课训练营的购买开关
     */
    private Boolean purchaseSwitch;
    /**
     * 售卖中小课的开营日期
     */
    private Date openDate;
    /**
     * 售卖小课的结营日期
     */
    private Date closeDate;
    /**
     * 售卖中小课的对应年份
     */
    private Integer sellingYear;
    /**
     * 售卖中小课的对应月份
     */
    private Integer sellingMonth;
    /**
     * 开营中小课的对应月份
     */
    private Integer learningMonth;
    /**
     * 新会员所在班级前缀
     */
    private String riseClassPrefix;
    /**
     * 小课训练营用户所在前缀
     */
    private String campClassPrefix;
    /**
     * 新会员的入群口令
     */
    private String riseEntryKey;
    /**
     * 小课训练营用户的入群口令
     */
    private String campEntryKey;
    /**
     * 当月新入会员的学号前缀
     */
    private String memberIdPrefix;
    /**
     * 该条数据记录是否生效
     */
    private Integer active;

}