package com.iquanwai.confucius.web.backend.dto;

import lombok.Data;

/**
 * Created by justin on 16/11/9.
 */
@Data
public class ErrorLogDto {
    private String cookie;
    private String result;
    private String url;
    private String browser;
}
