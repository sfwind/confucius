package com.iquanwai.confucius.biz.domain.fragmentation;

import com.iquanwai.confucius.biz.po.fragmentation.BusinessSchoolConfig;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampConfig;

public interface CacheService {
    void reload();

    BusinessSchoolConfig loadBusinessCollegeConfig();

    MonthlyCampConfig loadMonthlyCampConfig();

    void reloadMonthlyCampConfig();

    void reloadBusinessCollegeConfig();
}
