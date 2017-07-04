package com.iquanwai.confucius.web.course.dto.backend;

import lombok.Data;

import java.util.List;

/**
 * Created by justin on 17/6/26.
 */
@Data
public class SystemMsgDto {
    private List<Integer> profileIds;
    private String message;
    private String url;
}
