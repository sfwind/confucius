package com.iquanwai.confucius.web.course.dto;

import com.iquanwai.confucius.biz.po.Homework;
import lombok.Data;

/**
 * Created by justin on 16/9/3.
 */
@Data
public class PCHomeworkDto {
    private String openid;
    private Homework homework;
}
