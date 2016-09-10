package com.iquanwai.confucius.web.course.dto;

import com.iquanwai.confucius.biz.po.Course;
import com.iquanwai.confucius.biz.po.QuanwaiClass;
import lombok.Data;

/**
 * Created by justin on 16/9/10.
 */
@Data
public class SignupDto {
    private Integer remaining;
    private String qrcode;
    private Course course;
    private QuanwaiClass quanwaiClass;
}
