package com.iquanwai.confucius.biz.domain.fragmentation;

import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampConfig;

public interface CacheService {
    void reload();

    MonthlyCampConfig loadMonthlyCampConfig();

    void reloadMonthlyCampConfig();
}
