package com.iquanwai.confucius.biz.domain.performance;


import com.iquanwai.confucius.biz.po.performance.PagePerformance;
import com.iquanwai.confucius.biz.po.performance.Point;

import java.util.List;
import java.util.Map;

/**
 * Created by yongqiang.shen on 2017/3/2.
 */
public interface PerformanceService {
    /**
     * 收录前端性能数据
     */
    void add(PagePerformance po);

    /**
     * 获取折线图数据接口
     */
    Map<String,List<Point>>  queryLineChartData(String startTimeStr, String endTimeStr, int unitTimeAboutMinutes);

}