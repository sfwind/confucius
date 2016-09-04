package com.iquanwai.confucius.biz.po;

import lombok.Data;

import java.util.List;

/**
 * Created by justin on 16/8/25.
 */
@Data
public class Question {
    private int id;
    private String subject; //题干
    private String analysis; //题目分析
    private Integer point; //分值
    private boolean answered; //是否回答过
    private List<Choice> choiceList;
}
