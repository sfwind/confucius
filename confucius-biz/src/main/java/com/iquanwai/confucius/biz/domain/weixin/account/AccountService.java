package com.iquanwai.confucius.biz.domain.weixin.account;


import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.Region;

import java.util.List;

/**
 * Created by justin on 16/8/10.
 */
public interface AccountService {
    /**
     * 根据openid获取用户的详细信息
     * */
    Account getAccount(String openid, boolean realTime);

    /**
     * 更新个人信息
     * */
    void submitPersonalInfo(Account account);

    /**
     * 收集所有关注用户的信息
     * */
    void collectUsers();

    /**
     * 收集新关注用户的信息
     * */
    void collectNewUsers();

    /**
     * 获取所有的省份信息
     * */
    List<Region> loadAllProvinces();

    /**
     * 获取某省份的城市信息
     * */
    List<Region> loadCities();

    /**
     * 根据名字获取省
     */
    Region loadProvinceByName(String name);

    void submitRegion(String openId,String province, String city);

    /**
     * 根据名字获取城市
     */
    Region loadCityByName(String name);

    String USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token={access_token}&openid={openid}&lang=zh_CN";

    String GET_USERS_URL = "https://api.weixin.qq.com/cgi-bin/user/get?access_token={access_token}";
}
