package com.iquanwai.confucius.biz.domain.customer;

import com.iquanwai.confucius.biz.po.customer.Profile;

/**
 * Created by nethunder on 2017/2/8.
 */
public interface ProfileService {
    Profile getProfile(String openId);
    void submitPersonalCenterProfile(Profile profile);
    void submitPersonalInfo(Profile profile);
}