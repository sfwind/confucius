package com.iquanwai.confucius.web.course.dto;

import com.iquanwai.confucius.biz.po.systematism.CourseIntroduction;
import com.iquanwai.confucius.biz.po.systematism.QuanwaiClass;
import lombok.Data;

/**
 * Created by justin on 16/9/10.
 */
@Data
public class EntryDto {
    private String username;
    private String headUrl;
    private String memberId;
    private QuanwaiClass quanwaiClass;
    private CourseIntroduction course;
}
