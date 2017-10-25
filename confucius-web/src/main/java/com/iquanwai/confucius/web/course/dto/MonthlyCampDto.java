package com.iquanwai.confucius.web.course.dto;

import lombok.Data;

@Data
public class MonthlyCampDto {
    private String marKSellingMemo; // 打点用到的，2017-10
    private Integer currentCampMonth; // 当前小课训练营配置月份
    private Integer campMonthProblemId; // 当前小课训练营跳转月份

}
