package com.iquanwai.confucius.web.performance.dto;

import com.iquanwai.confucius.biz.po.performance.PagePerformance;
import com.iquanwai.confucius.biz.po.performance.PersonalPerformance;
import lombok.Data;

/**
 * Created by shen on 17/3/3.
 */
@Data
public class PagePerformanceDto {
    private PagePerformance page;
    private PersonalPerformance personal;
}
