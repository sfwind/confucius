package com.iquanwai.confucius.web.course.dto.backend;

import lombok.Data;

import java.util.List;

/**
 * Created by justin on 16/11/11.
 */
@Data
public class NoticeMsgDto {
    private String task;
    private String first;
    private String remark;
    private List<String> openids;
}
