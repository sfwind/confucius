package com.iquanwai.confucius.biz.domain.course.signup;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by nethunder on 2017/4/7.
 */
public interface RiseMemberCountRepo {
    // 初始化报名参数
    void init();
    // 预报名
    Pair<Integer,String> prepareSignup(String openId);
    // 退出预报名
    void quitSignup(String openId,Integer memberTypeId);
}
