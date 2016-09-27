package com.iquanwai.confucius.biz.domain.weixin.account;


import com.iquanwai.confucius.biz.po.Account;

/**
 * Created by justin on 16/8/10.
 */
public interface AccountService {
    /**
     * 根据openid获取用户的详细信息
     * */
    Account getAccount(String openid, boolean realTime);

    /**
     * 收集所有关注用户的信息
     * */
    void collectUsers();

    String USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token={access_token}&openid={openid}&lang=zh_CN";

    String GET_USERS_URL = "https://api.weixin.qq.com/cgi-bin/user/get?access_token={access_token}";
}
