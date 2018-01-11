package com.iquanwai.confucius.biz.domain.weixin.account;


import com.iquanwai.confucius.biz.exception.NotFollowingException;
import com.iquanwai.confucius.biz.po.Account;
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
    String LIST_BLACKLIST_URL = "https://api.weixin.qq.com/cgi-bin/tags/members/getblacklist?access_token={access_token}";
    String BATCH_BALCKLIST_URL = "https://api.weixin.qq.com/cgi-bin/tags/members/batchblacklist?access_token={access_token}";
    String UNBATCH_BACKLIST_URL = "https://api.weixin.qq.com/cgi-bin/tags/members/batchunblacklist?access_token={access_token}";

    /**
     * 根据openid获取用户的详细信息
     */
    Account getAccount(String openid, boolean realTime) throws NotFollowingException;

    /**
     * 根据riseid获取用户的详细信息
     */
    Profile getProfileByRiseId(String riseId);

    /**
     * 根据profile id 列表获取用户的详细信息
     */
    List<Profile> getProfiles(List<Integer> profileIds);

    /**
     * 从微信实时获取头像信息
     */
    String getRealHeadImgUrlFromWeixin(String openId) throws NotFollowingException;

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
     * 根据openid获取用户详情
     */
    Profile getProfile(String openid);

    /**
     * 取消关注
     */
    void unfollow(String openid);

    /**
     * 根据昵称模糊查询用户的详细信息(200条)
     */
    List<Profile> loadProfilesByNickName(String nickName);

    /**
     * 根据昵称模糊查询用户的详细信息(所有)
     */
    List<Profile> loadAllProfilesByNickName(String nickName);

    /**
     * 根据学号查询 RIseClassMember 记录，不进行 Active 字段区分
     */
    Profile loadProfileByMemberId(String memberId);

    Boolean hasPrivilegeForBusinessSchool(Integer profileId);

    /**
     * 获取黑名单列表
     * (该接口一次最多返回10000条数据)
     */
    List<String> loadBlackListOpenIds();

    /**
     * 批量拉黑用户
     * @param openidList 拉黑用户列表
     */
    boolean batchBlackList(List<String> openidList);

    /**
     * 取消拉黑用户
     * @param openidList 取消拉黑用户列表
     */
    boolean batchUnBlackList(List<String> openidList);

    Integer loadUserScheduleCategory(Integer profileId);

    /**
     * 根据unionId获取用户详情
     */
    Profile queryByUnionId(String unionid);

    int updateHeadImageUrl(Integer profileId, String headImgUrl);


    int initProfileAndFollowUser(String unionId, String nickName, String avatarUrl, Integer gender);
}
