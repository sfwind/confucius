package com.iquanwai.confucius.biz.po.systematism;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 16/9/3.
 */
@Data
public class QuestionSubmit {
    private int id;
    private Integer questionId; //问题id
    private String submitOpenid; //提交人openid
    private Integer submitProfileId; //提交人id
    private Integer classId;  //班级id
    private Date submitTime;  //提交时间
    private String submitAnswer; //提交答案的序号，多选用逗号隔开
    private Integer score; //作业分数
    private Integer isRight;

}
