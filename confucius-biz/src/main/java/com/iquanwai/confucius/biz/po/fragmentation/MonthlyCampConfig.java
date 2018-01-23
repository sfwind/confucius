package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

import java.util.Date;

@Data
public class MonthlyCampConfig {

    private Integer id;
    /**
     * 专项课的购买开关
     */
    private Boolean purchaseSwitch;
    /**
     * 售卖中专项课开营日期
     */
    private Date openDate;
    /**
     * 售卖中专项课结营日期
     */
    private Date closeDate;
    /**
     * 售卖中专项课的对应年份
     */
    private Integer sellingYear;
    /**
     * 售卖中专项课的对应月份
     */
    private Integer sellingMonth;
    /**
     * 该条数据记录是否生效
     */
    private Integer active;

}
