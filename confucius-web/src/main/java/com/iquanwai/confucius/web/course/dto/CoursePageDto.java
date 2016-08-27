package com.iquanwai.confucius.web.course.dto;


import com.iquanwai.confucius.biz.dao.po.Course;
import lombok.Data;

/**
 * Created by justin on 16/8/25.
 */
@Data
public class CoursePageDto{
    private String openid;
    private String username;
    private int week;
    private int userProgress; //用户课程进度序号
    private Course course;

}
