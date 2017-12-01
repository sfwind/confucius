package com.iquanwai.confucius.biz.domain.fragmentation;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iquanwai.confucius.biz.dao.fragmentation.BusinessSchoolConfigDao;
import com.iquanwai.confucius.biz.dao.fragmentation.MonthlyCampConfigDao;
import com.iquanwai.confucius.biz.po.fragmentation.BusinessSchoolConfig;
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
    private BusinessSchoolConfigDao businessSchoolConfigDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private MonthlyCampConfig monthlyCampConfig;
    private BusinessSchoolConfig businessSchoolConfig;

    @PostConstruct
    public void init() {
        monthlyCampConfig = monthlyCampConfigDao.loadActiveConfig();
        logger.info("monthly camp configuration init complete");
        businessSchoolConfig = businessSchoolConfigDao.loadActiveConfig();
        logger.info("business college configuration init complete");
    }

    @Override
    public MonthlyCampConfig loadMonthlyCampConfig() {
        return JSONObject.parseObject(JSON.toJSONString(monthlyCampConfig), MonthlyCampConfig.class);
    }

    @Override
    public BusinessSchoolConfig loadBusinessCollegeConfig() {
        return JSONObject.parseObject(JSON.toJSONString(businessSchoolConfig), BusinessSchoolConfig.class);
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
        businessSchoolConfig = businessSchoolConfigDao.loadActiveConfig();
    }

}
