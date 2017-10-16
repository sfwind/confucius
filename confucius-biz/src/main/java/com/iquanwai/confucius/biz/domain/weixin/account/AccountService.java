package com.iquanwai.confucius.biz.domain.weixin.account;


import com.iquanwai.confucius.biz.exception.NotFollowingException;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.Region;
import com.iquanwai.confucius.biz.po.common.customer.Profile;

import java.util.List;

/**
 * Created by justin on 16/8/10.
 */
public interface AccountService {

    String USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token={access_token}&openid={openid}&lang=zh_CN";

    String GET_USERS_URL = "https://api.weixin.qq.com/cgi-bin/user/get?access_token={access_token}";

    String GET_NEXT_USERS_URL = "https://api.weixin.qq.com/cgi-bin/user/get?access_token={access_token}&next_openid={next_openid}";

    String PC_USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token={access_token}&openid={openid}&lang=zh_CN";

    /**
     * 根据openid获取用户的详细信息
     */
    Account getAccount(String openid, boolean realTime) throws NotFollowingException;

    Profile getProfileByRiseId(String riseId);

    List<Profile> getProfiles(List<Integer> profileIds);

    /**
     * 收集所有关注用户的信息
     */
    void collectUsers();

    /**
     * 收集新关注用户的信息
     */
    void collectNewUsers();

    /**
     * 收集关注用户的信息
     */
    void collectNext(String openid);

    /**
     * 根据openid获取用户详情
     */
    Profile getProfile(String openid, boolean realTime);

    /**
     * 根据openid获取用户详情
     */
    Profile getProfile(Integer profileId);

    /**
     * 取消关注
     */
    void unfollow(String openid);

    /**
     * 更新riseMember状态
     * */
    void updateRiseMember(String openid, Integer riseMember);

    List<Profile> loadProfilesByNickName(String nickName);

    Boolean hasPrivilegeForBusinessSchool(Integer profileId);
}
