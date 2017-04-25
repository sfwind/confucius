package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;

/**
 * Created by justin on 17/3/29.
 */
@Data
public class ConfigDto {
    private String projectId;
    private String key;
    private String value;
    private boolean edit = false;
    private boolean display = true;
}
