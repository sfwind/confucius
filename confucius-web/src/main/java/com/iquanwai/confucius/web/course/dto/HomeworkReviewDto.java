package com.iquanwai.confucius.web.course.dto;

import lombok.Data;

/**
 * Created by justin on 16/9/22.
 */
@Data
public class HomeworkReviewDto {
    private int homeworkId;
    private int classId;
    private String openid;
    private boolean excellent;
    private boolean fail;
}
