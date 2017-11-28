package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

import java.util.Date;

@Data
public class MonthlyCampConfig {

    private Integer id;
    /**
     * 训练营的购买开关
     */
    private Boolean purchaseSwitch;
    /**
     * 售卖中训练营开营日期
     */
    private Date openDate;
    /**
     * 售卖中训练营结营日期
     */
    private Date closeDate;
    /**
     * 售卖中训练营的对应年份
     */
    private Integer sellingYear;
    /**
     * 售卖中训练营的对应月份
     */
    private Integer sellingMonth;
    /**
     * 开营中训练营的对应月份
     */
    private Integer learningMonth;
    /**
     * 新会员所在班级前缀
     */
    private String riseClassPrefix;
    /**
     * 训练营用户所在前缀
     */
    private String campClassPrefix;
    /**
     * 新会员的入群口令
     */
    @Deprecated
    private String riseEntryKey;
    /**
     * 训练营用户的入群口令
     */
    @Deprecated
    private String campEntryKey;
    /**
     * 当月新入会员的学号前缀
     */
    @Deprecated
    private String memberIdPrefix;
    /**
     * 该条数据记录是否生效
     */
    private Integer active;

}
