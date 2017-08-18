package com.iquanwai.confucius.biz.domain.course.signup;

import com.iquanwai.confucius.biz.po.common.customer.CourseReductionActivity;

/**
 * Created by nethunder on 2017/8/17.
 */
public interface CourseReductionService {

    /**
     * 获取用户最近参加的推广活动
     * @param profileId 用户id
     * @return 推广活动
     */
    CourseReductionActivity loadRecentCourseReduction(Integer profileId, Integer problemId);
}
