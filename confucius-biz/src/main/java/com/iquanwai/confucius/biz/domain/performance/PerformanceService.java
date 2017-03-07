package com.iquanwai.confucius.biz.domain.performance;


import com.iquanwai.confucius.biz.domain.performance.entity.PageAnalyticsDto;
import com.iquanwai.confucius.biz.po.performance.PagePerformance;
import com.iquanwai.confucius.biz.po.performance.PageUrl;

import java.util.List;

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
    PageAnalyticsDto queryLineChartData(int urlId, String startTimeStr, String endTimeStr, int unitTimeAboutMinutes);

    /**
     * 获取所有上报的Url
     */
    List<PageUrl> queryUrlList ();
}