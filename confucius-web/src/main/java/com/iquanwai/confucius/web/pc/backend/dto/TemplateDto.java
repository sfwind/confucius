package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class TemplateDto {
    private Integer templateId;
    private String comment;
    private String first;
    private String remark;
    private String url;
    private String keyword1;
    private String keyword2;
    private String keyword3;
    private List<String> openIds;
}
