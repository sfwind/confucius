package com.iquanwai.confucius.biz.domain.course.signup;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by justin on 16/9/30.
 */
public interface ClassMemberCountRepo {
    /**
     * 是否报名
     * @param openid
     * TODO:只能同时报一门课
     * */
    boolean isEntry(String openid);

    /**
     * 为用户预报名，占一个班级名额
     * @param openid
     * @param courseId
     * @return
     */
    Pair<Integer, Integer> prepareSignup(String openid, Integer courseId);

    /**
     * 用户放弃报名
     * @param openid
     * @return
     */
    void quitClass(String openid);
}
