package com.iquanwai.confucius.biz.po;

import lombok.Data;

import java.util.List;

/**
 * Created by justin on 16/8/25.
 */
@Data
public class Question {
    private int id;
    private Integer courseId; //课程id
    private String subject; //题干
    private Integer type; //题目类型（1-单选题，2-多选题）
    private Integer emotionType; //题目类型（1-单选题，2-多选题）
    private String analysis; //题目分析
    private Integer analysisType; //分析展现类型（1-文字，2-图片，3-语音）
    private Integer point; //分值
    private boolean answered; //是否回答过
    private List<Choice> choiceList;
}
