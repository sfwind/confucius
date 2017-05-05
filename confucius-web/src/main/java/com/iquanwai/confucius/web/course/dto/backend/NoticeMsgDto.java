package com.iquanwai.confucius.web.course.dto.backend;

import lombok.Data;

import java.util.List;

/**
 * Created by justin on 16/11/11.
 */
@Data
public class NoticeMsgDto {
    private String first;
    private String remark;
    private String keyword1;
    private String keyword2;
    private String keyword3;
    private String messageId;
    private List<String> openids;
    private String url;
}
