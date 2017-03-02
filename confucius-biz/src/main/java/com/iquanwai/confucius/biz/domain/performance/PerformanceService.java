package com.iquanwai.confucius.biz.domain.performance;


import com.iquanwai.confucius.biz.po.performance.PagePerformance;

/**
 * Created by yongqiang.shen on 2017/3/2.
 */
public interface PerformanceService {
    /**
     * 收录前端性能数据
     */
    void add(PagePerformance po);

}