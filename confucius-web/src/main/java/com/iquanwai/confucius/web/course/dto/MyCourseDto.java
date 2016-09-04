package com.iquanwai.confucius.web.course.dto;

import com.iquanwai.confucius.biz.dao.po.Course;
import lombok.Data;

/**
 * Created by justin on 16/9/4.
 */
@Data
public class MyCourseDto {
    private String openid;
    private String username;
    private Double myProgress;
    private Double courseProgress;
    private Course course;

}
