package com.iquanwai.confucius.web.course.dto;


import com.iquanwai.confucius.biz.po.Course;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 16/8/25.
 */
@Data
public class CoursePageDto{
    private Course course;
    private int week; //当前周数
    private String topic; //周主题
    private List<WeekIndexDto> weekIndex; //周索引

}
