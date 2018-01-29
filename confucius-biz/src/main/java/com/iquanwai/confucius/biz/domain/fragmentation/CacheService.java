package com.iquanwai.confucius.biz.domain.fragmentation;

import com.iquanwai.confucius.biz.po.fragmentation.BusinessSchoolConfig;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampConfig;

public interface CacheService {
    BusinessSchoolConfig loadBusinessCollegeConfig();

    MonthlyCampConfig loadMonthlyCampConfig();

    void reloadMonthlyCampConfig();

    void reloadBusinessCollegeConfig();
}
