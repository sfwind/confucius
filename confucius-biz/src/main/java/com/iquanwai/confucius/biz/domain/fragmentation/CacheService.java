package com.iquanwai.confucius.biz.domain.fragmentation;

import com.iquanwai.confucius.biz.po.fragmentation.BusinessCollegeConfig;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampConfig;

public interface CacheService {
    void reload();

    BusinessCollegeConfig loadBusinessCollegeConfig();

    MonthlyCampConfig loadMonthlyCampConfig();

    void reloadMonthlyCampConfig();

    void reloadBusinessCollegeConfig();
}
