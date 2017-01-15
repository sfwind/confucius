package com.iquanwai.confucius.web.pc.dto;

import lombok.Data;

/**
 * Created by nethunder on 2017/1/13.
 */
@Data
public class HomeworkDto {
    private Integer type; // 11，应用训练 21，挑战任务
    private Integer planId; // 计划id
    private Integer workId; // 对应的id
    private String title; // 主题
    private Integer score; // 分数
    private Boolean unlocked;
    private Integer status; //题目状态（0-未完成，1-已完成, 3-选做）
}
