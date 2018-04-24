package com.iquanwai.confucius.web.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProfileSetDto {
    private List<Integer> profiles;
    private String key;
    private Object value;
}
