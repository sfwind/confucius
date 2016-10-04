package com.iquanwai.confucius.web.course.dto;


import com.iquanwai.confucius.biz.po.Course;
import lombok.Data;

/**
 * Created by justin on 16/8/25.
 */
@Data
public class CoursePageDto{
    private Course course;
    private int week;
    private String topic;

}
