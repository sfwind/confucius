package com.iquanwai.confucius.web.course.dto.backend;

import lombok.Data;

import java.util.List;

/**
 * Created by justin on 17/7/28.
 */
@Data
public class RefreshLoginUserDto {
    private List<String> unionIds;
}
