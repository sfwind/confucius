package com.iquanwai.confucius.biz.domain.customer;

import com.iquanwai.confucius.biz.po.common.customer.Profile;

/**
 * Created by nethunder on 2017/2/8.
 */
public interface ProfileService {

    /**
     * 在个人中心里提交用户信息
     */
    void submitPersonalCenterProfile(Profile profile);

    /**
     * 在报名以及毕业证书前的那个页面提交用户信息
     */
    void submitPersonalInfo(Profile profile, Boolean risePoint);
}