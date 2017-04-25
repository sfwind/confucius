package com.iquanwai.confucius.web.course.dto;

import com.iquanwai.confucius.biz.po.systematism.CourseIntroduction;
import lombok.Data;

/**
 * Created by justin on 16/9/4.
 */
@Data
public class MyCourseDto {
    private Double myProgress;
    private Double courseProgress;
    private CourseIntroduction course;

}
