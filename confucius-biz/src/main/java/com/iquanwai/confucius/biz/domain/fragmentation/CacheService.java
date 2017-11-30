package com.iquanwai.confucius.biz.domain.fragmentation;

import com.iquanwai.confucius.biz.po.fragmentation.BusinessCollegeConfig;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampConfig;

public interface CacheService {
    BusinessCollegeConfig loadBusinessCollegeConfig();

    void reload();

    MonthlyCampConfig loadMonthlyCampConfig();

    void reloadMonthlyCampConfig();

    void reloadBusinessCollegeConfig();
}
