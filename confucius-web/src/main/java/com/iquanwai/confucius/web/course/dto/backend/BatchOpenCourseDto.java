package com.iquanwai.confucius.web.course.dto.backend;

import lombok.Data;

import java.util.Date;

/**
 * Created by 三十文 on 2017/9/20
 */
@Data
public class BatchOpenCourseDto {

    private Integer month;
    private Integer problemId;
    private Date startDate;
    private Date closeDate;

}
