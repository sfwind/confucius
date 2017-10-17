package com.iquanwai.confucius.biz.domain.course.signup;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by nethunder on 2017/4/7.
 */
@Deprecated
public interface RiseMemberCountRepo {
    // 初始化报名参数
    void init();

    void reload();

    // 预报名
    Pair<Integer,String> prepareSignup(Integer profileId);

    Pair<Integer,String> prepareSignup(Integer profileId, Boolean hold);

    // 退出预报名
    void quitSignup(Integer profileId,Integer memberTypeId);

    Integer getRemindingCount();
}
