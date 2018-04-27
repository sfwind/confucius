package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;

@Data
public class PreviewDto {
    /**
     * 章
     */
    private Integer chapter;
    /**
     * 节
     */
    private Integer section;
    /**
     * 内容
     */
    private String description;

}
