package com.iquanwai.confucius.biz.domain.fragmentation.point;

import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by justin on 16/12/14.
 */
@Service
public class PointMangerImpl implements PointManger {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private OperationLogService operationLogService;


    @Override
    public void risePoint(Integer planId, Integer increment) {
        ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
        if (improvementPlan != null) {
            improvementPlanDao.updatePoint(planId, improvementPlan.getPoint() + increment);
        }
    }

    @Override
    public void riseCustomerPoint(Integer profileId, Integer increment) {
        Profile profile = profileDao.load(Profile.class, profileId);
        if (profile != null) {
            profileDao.updatePoint(profileId, profile.getPoint() + increment);
            operationLogService.profileSet(profileId, "point", profile.getPoint() + increment);
        }
    }

}
