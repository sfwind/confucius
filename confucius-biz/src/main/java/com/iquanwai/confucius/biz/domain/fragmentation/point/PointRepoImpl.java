package com.iquanwai.confucius.biz.domain.fragmentation.point;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.fragmentation.ChallengeSubmitDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/14.
 */
@Service
public class PointRepoImpl implements PointRepo {
    public static Map<Integer, Integer> score = Maps.newHashMap();

    @PostConstruct
    public void initPoint(){
        List<Integer> scores = ConfigUtils.getWorkScoreList();
        for (int i = 0; i < scores.size(); i++) {
            score.put(i+1,scores.get(i));
        }
    }


    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Override
    public void risePoint(Integer planId, Integer increment) {
        ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
        if(improvementPlan!=null){
            improvementPlanDao.updatePoint(planId, improvementPlan.getPoint()+increment);
        }
    }
}
