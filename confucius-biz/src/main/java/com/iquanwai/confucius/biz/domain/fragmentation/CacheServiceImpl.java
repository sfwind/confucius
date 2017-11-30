package com.iquanwai.confucius.biz.domain.fragmentation;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iquanwai.confucius.biz.dao.fragmentation.BusinessCollegeConfigDao;
import com.iquanwai.confucius.biz.dao.fragmentation.MonthlyCampConfigDao;
import com.iquanwai.confucius.biz.po.fragmentation.BusinessCollegeConfig;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CacheServiceImpl implements CacheService {

    @Autowired
    private MonthlyCampConfigDao monthlyCampConfigDao;
    @Autowired
    private BusinessCollegeConfigDao businessCollegeConfigDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private MonthlyCampConfig monthlyCampConfig;
    private BusinessCollegeConfig businessCollegeConfig;

    @PostConstruct
    public void init() {
        monthlyCampConfig = monthlyCampConfigDao.loadActiveConfig();
        logger.info("monthly camp configuration init complete");
        businessCollegeConfig = businessCollegeConfigDao.loadActiveConfig();
        logger.info("business college configuration init complete");
    }

    @Override
    public MonthlyCampConfig loadMonthlyCampConfig() {
        return JSONObject.parseObject(JSON.toJSONString(monthlyCampConfig), MonthlyCampConfig.class);
    }

    @Override
    public BusinessCollegeConfig loadBusinessCollegeConfig() {
        return JSONObject.parseObject(JSON.toJSONString(businessCollegeConfig), BusinessCollegeConfig.class);
    }

    @Override
    public void reload() {
        init();
    }

    @Override
    public void reloadMonthlyCampConfig() {
        monthlyCampConfig = monthlyCampConfigDao.loadActiveConfig();
    }

    @Override
    public void reloadBusinessCollegeConfig() {
        businessCollegeConfig = businessCollegeConfigDao.loadActiveConfig();
    }

}
