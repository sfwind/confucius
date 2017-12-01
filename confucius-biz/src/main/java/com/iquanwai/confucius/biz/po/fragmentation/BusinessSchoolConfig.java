package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

import java.util.Date;

@Data
public class BusinessSchoolConfig {

    private Integer id;
    private Boolean purchaseSwitch; // 商学院购买开关
    private Date openDate; // 商学院开启日期
    private Integer sellingYear; // 当前售卖年份
    private Integer sellingMonth; // 当前售卖月份
    private Boolean active; // 是否生效

}
