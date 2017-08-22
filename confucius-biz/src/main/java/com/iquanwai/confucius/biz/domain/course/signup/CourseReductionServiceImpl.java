package com.iquanwai.confucius.biz.domain.course.signup;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.common.customer.CourseReductionActivityDao;
import com.iquanwai.confucius.biz.dao.common.customer.PromotionLevelDao;
import com.iquanwai.confucius.biz.dao.common.customer.PromotionUserDao;
import com.iquanwai.confucius.biz.po.common.customer.CourseReductionActivity;
import com.iquanwai.confucius.biz.po.common.customer.PromotionLevel;
import com.iquanwai.confucius.biz.util.PromotionConstants;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2017/8/17.
 */
@Service
public class CourseReductionServiceImpl implements CourseReductionService {
    @Autowired
    private CourseReductionActivityDao courseReductionActivityDao;
    @Autowired
    private PromotionUserDao promotionUserDao;
    @Autowired
    private PromotionLevelDao promotionLevelDao;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public CourseReductionActivity loadRecentCourseReduction(Integer profileId,Integer problemId) {
        List<PromotionLevel> promotionLevels = promotionLevelDao.loadByRegex(PromotionConstants.Activities.CourseReduction, profileId);
        if (CollectionUtils.isEmpty(promotionLevels)) {
            return null;
        }
        List<String> activities = Lists.newArrayList();
        promotionLevels.forEach(level -> {
            String activity = level.getActivity();
            String[] split = activity.split("_");
            if (split.length < 1) {
                logger.error("异常，课程减免活动数据异常", level);
            } else {
                String realActivity = split[0];
                if (!activities.contains(realActivity)) {
                    activities.add(realActivity);
                }
            }
        });
        List<CourseReductionActivity> list = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(activities)) {
            list =  courseReductionActivityDao
                    .loadReductions(activities)
                    .stream()
                    .filter(activity -> activity.getProblemId() == null ||
                            activity.getProblemId().equals(problemId))
                    .collect(Collectors.toList());
        }
        promotionLevels.sort((o1, o2) -> o2.getId() - o1.getId());
        for (PromotionLevel level : promotionLevels) {
            for (CourseReductionActivity courseReductionActivity : list) {
                if (level.getActivity().contains(courseReductionActivity.getActivity())) {
                    // 取出第一个匹配到的courseReduction,因为有可能扫码后活动取消了
                    // courseReduction-zlj_02 contains courseReduction-zlj
                    return courseReductionActivity;
                }
            }
        }
        // 上面没有return的话，则return null
        return null;
    }

}
