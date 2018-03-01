package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

@Data
public class MonthlyCampOrder {

    private Integer id;
    private String orderId; // 主订单 id
    private Integer profileId; // 用户 id
    private Integer year; // 年份
    private Integer month; // 月份
    private Boolean entry; // 是否已经报名
    private Integer isDel; // 是否已经过期

}
