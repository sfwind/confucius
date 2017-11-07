package com.iquanwai.confucius.web.course.dto.backend;

import lombok.Data;

@Data
public class MonthlyCampProcessDto {
    private String marKSellingMemo; // 打点用到的，2017-10
    private Integer currentCampMonth; // 当前小课训练营配置月份
    private Integer campMonthProblemId; // 当前小课训练营跳转月份

    private Integer sourceYear;
    private Integer sourceMonth;
    private Integer targetYear;
    private Integer targetMonth;
}
