package com.iquanwai.confucius.web.course.dto;

import com.iquanwai.confucius.biz.po.CourseIntroduction;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 16/10/27.
 */
@Data
public class AllCourseDto {
    private List<MyCourseDto> myCourses;
    private List<CourseIntroduction> otherCourses;
}
