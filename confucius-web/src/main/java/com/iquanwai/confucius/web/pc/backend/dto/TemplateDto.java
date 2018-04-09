package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;


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
    private String openIds;
    /**
     * 排除人数
     */
    private String excludeOpenIds;
    private String remarkColor;
    private Boolean forcePush;
    private Boolean isMime;
    private String source;
}
