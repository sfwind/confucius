package com.iquanwai.confucius.biz.domain.fragmentation;

import com.iquanwai.confucius.biz.po.fragmentation.BusinessSchoolConfig;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampConfig;

public interface CacheService {
    BusinessSchoolConfig loadBusinessCollegeConfig(Integer memberTypeId);

    MonthlyCampConfig loadMonthlyCampConfig();

    void reload();

    void reloadMonthlyCampConfig();

    void reloadBusinessCollegeConfig();
}
