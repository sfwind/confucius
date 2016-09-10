package com.iquanwai.confucius.web.course.dto;


import com.iquanwai.confucius.biz.po.Course;
import lombok.Data;

/**
 * Created by justin on 16/8/25.
 */
@Data
public class CoursePageDto{
    private int week;
    private Course course;

}
