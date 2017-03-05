package com.iquanwai.confucius.biz.domain.performance.entity;

/**
 * Created by shen on 17/3/5.
 */

import lombok.Data;

import java.util.List;

@Data
public class PageAnalyticsDto {
    private List<Point> pvList;
    private List<Point> loadList;
    private List<Point> interactiveList;
    private List<Point> ttfbList;
    private List<Point> paintList;
}
