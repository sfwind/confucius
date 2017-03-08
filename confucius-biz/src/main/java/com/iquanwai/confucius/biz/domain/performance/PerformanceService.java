package com.iquanwai.confucius.biz.domain.performance;


import com.iquanwai.confucius.biz.domain.performance.entity.PageAnalyticsDto;
import com.iquanwai.confucius.biz.domain.performance.entity.PersonalAnalyticsDto;
import com.iquanwai.confucius.biz.po.performance.PagePerformance;
import com.iquanwai.confucius.biz.po.performance.PageUrl;
import com.iquanwai.confucius.biz.po.performance.PersonalPerfKey;
import com.iquanwai.confucius.biz.po.performance.PersonalPerformance;

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
     * 收录主动上报的个性化数据
     */
    void addPersonalPerf(PersonalPerformance po);

    /**
     * 获取折线图数据接口
     */
    PageAnalyticsDto queryLineChartData(int urlId, String startTimeStr, String endTimeStr, int unitTimeAboutMinutes);


    /**
     * 获取主动上报的折线图数据接口
     */
    PersonalAnalyticsDto queryPersonalLineChartData(int keyId, String startTimeStr, String endTimeStr, int unitTimeAboutMinutes);

    /**
     * 获取所有上报的Url
     */
    List<PageUrl> queryUrlList ();


    /**
     * 获取所有上报的Url
     */
    List<PersonalPerfKey> queryKeyList ();
}