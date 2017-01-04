package com.iquanwai.confucius.biz.domain.course.signup;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by justin on 16/9/30.
 */
public interface ClassMemberCountRepo {
    /**
     * 课程人数初始化
     * */
    void initClass();

    /**
     * 是否报名某课程
     * @param openid 学员id
     * @param courseId 课程id
     * */
    boolean isEntry(String openid, Integer courseId);

    /**
     * 为用户预报名，占一个班级名额
     * @param openid 学员id
     * @param courseId 课程id
     * @return
     */
    Pair<Integer, Integer> prepareSignup(String openid, Integer courseId);

    /**
     * 用户放弃报名
     * @param openid 学员id
     * @param classId 班级id
     * @return
     */
    void quitClass(String openid, Integer classId);
}
