package com.iquanwai.confucius.web.payment.dto;

import lombok.Data;

@Data
public class MonthlyCampProcessDto {
    private String markSellingMemo; // 打点用到的，2017-10
    private Integer currentCampMonth; // 当前专项课配置月份
    private Integer campMonthProblemId; // 当前专项课跳转月份

    private Integer sourceYear;
    private Integer sourceMonth;
    private Integer targetYear;
    private Integer targetMonth;
}
