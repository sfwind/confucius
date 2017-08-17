package com.iquanwai.confucius.biz.domain.course.signup;

import com.iquanwai.confucius.biz.po.common.customer.CourseReductionActivity;

import java.util.List;

/**
 * Created by nethunder on 2017/8/17.
 */
public interface CourseReductionService {

    /**
     * 获取用户参加的价格最低的推广活动
     * @param profileId 用户id
     * @return 推广活动
     */
    CourseReductionActivity loadMinPriceCourseReduction(Integer profileId,Integer problemId);

    /**
     * 查看该用户是否能参加减免活动
     * @param profileId 用户id
     * @return 该用户参加的课程减免活动
     */
    List<CourseReductionActivity> loadCourseReductions(Integer profileId,Integer problemId);
}
