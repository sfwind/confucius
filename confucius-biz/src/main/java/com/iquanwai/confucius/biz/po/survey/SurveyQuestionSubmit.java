package com.iquanwai.confucius.biz.po.survey;

import lombok.Data;

/**
 * Created by nethunder on 2017/1/17.
 */
@Data
public class SurveyQuestionSubmit {
    private Integer id; // 问题提交id
    private Integer surveySubmitId; // 问卷提交id
    private Integer activity; // 问卷id
    private String openId; //用户openId

//    private Integer type; // 题目类型 1-单选 2-多选 3-填空 4-多行填空
//    private Integer sequence; // 问卷中的题目顺序，即第几道题
    private String questionLabel;
    private String content; // 提交的内容
}
