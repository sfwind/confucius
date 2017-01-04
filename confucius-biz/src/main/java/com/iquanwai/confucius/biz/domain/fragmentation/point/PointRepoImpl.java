package com.iquanwai.confucius.biz.domain.fragmentation.point;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/14.
 */
@Service
public class PointRepoImpl implements PointRepo {
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
