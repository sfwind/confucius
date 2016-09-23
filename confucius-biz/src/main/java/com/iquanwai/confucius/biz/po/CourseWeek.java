package com.iquanwai.confucius.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/9/22.
 */
@Data
public class CourseWeek {
    private int id;
    private Integer courseId; //课程id
    private Integer sequence; //第几周
    private String topic; //周主题

}
