package com.iquanwai.confucius.web.course.dto;

import lombok.Data;

/**
 * Created by justin on 16/10/3.
 */
@Data
public class InfoSubmitDto {
    private String mobileNo;  //手机号
    private String email;  //邮箱
    private String industry; //行业
    private String function; //职业
    private String workingLife; //工作年限
    private Integer courseId; //课程id
    private String realName; //真名
}
