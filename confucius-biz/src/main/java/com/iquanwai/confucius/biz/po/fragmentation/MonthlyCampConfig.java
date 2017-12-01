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
     * 训练营用户所在前缀
     */
    private String campClassPrefix;
    /**
     * 该条数据记录是否生效
     */
    private Integer active;

}
