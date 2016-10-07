package com.iquanwai.confucius.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 16/9/3.
 */
@Data
public class HomeworkSubmit {
    private int id;
    private Integer homeworkId; //作业id
    private String submitOpenid; //提交人openid
    private Integer classId;  //班级id
    private Date submitTime;  //提交时间
    private String submitContent; //提交内容
    private Integer score; //作业分数
    private String remark; //作业评语
    private String submitUrl; //提交url
    private String shortUrl; //提交短链接
}
