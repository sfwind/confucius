package com.iquanwai.confucius.web.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by justin on 17/7/28.
 */
@Data
public class RefreshLoginUserDto {
    private List<String> unionIds;
}
