package com.iquanwai.confucius.web.course.dto.backend;

import lombok.Data;

import java.util.List;

/**
 * Created by justin on 2017/9/24.
 */
@Data
public class CustomerMsgDto {
    private List<String> openids;
    private String message;
}
