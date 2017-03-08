package com.iquanwai.confucius.biz.domain.performance.entity;

import lombok.Data;

import java.util.List;

/**
 * Created by shen on 17/3/8.
 */
@Data
public class PersonalAnalyticsDto {
    private List<Point> timeList;
}
