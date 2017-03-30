package com.iquanwai.confucius.biz.domain.performance.entity;

import com.iquanwai.confucius.biz.po.performance.PagePerformance;
import com.iquanwai.confucius.biz.po.performance.PersonalPerformance;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by shen on 17/3/5.
 */
@Data
public class DataSourceForPoint {
    private List<PagePerformance> pagePerformances;
    private List<PersonalPerformance> personalPerformances;
    private LocalDateTime time;
}
