package com.iquanwai.confucius.biz.domain.fragmentation.point;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/12/14.
 */
@Service
public class PointRepoImpl implements PointRepo {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private ProfileDao profileDao;

    public static Map<Integer, Integer> score = Maps.newHashMap();


    @PostConstruct
    public void initPoint() {
        List<Integer> scores = ConfigUtils.getWorkScoreList();
        logger.info("score init");
        for (int i = 0; i < scores.size(); i++) {
            score.put(i + 1, scores.get(i));
        }
        logger.info("score map:{}", score);
    }


    @Override
    public void risePoint(Integer planId, Integer increment) {
        ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
        if(improvementPlan!=null){
            improvementPlanDao.updatePoint(planId, improvementPlan.getPoint()+increment);
        }
    }

    @Override
    public void riseCustomerPoint(String openId,Integer increment){
        Profile profile = profileDao.queryByOpenId(openId);
        if(profile!=null){
            profileDao.updatePoint(openId,profile.getPoint() + increment);
        }
    }

    @Override
    public void reloadScore(){
        score.clear();
        this.initPoint();
    }

}
