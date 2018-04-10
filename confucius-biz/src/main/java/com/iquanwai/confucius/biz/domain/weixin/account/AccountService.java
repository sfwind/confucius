package com.iquanwai.confucius.biz.domain.weixin.account;


import com.iquanwai.confucius.biz.domain.weixin.api.WeiXinResult;
import com.iquanwai.confucius.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.Role;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.util.page.Page;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 16/8/10.
 */
public interface AccountService {

    String USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token={access_token}&openid={openid}&lang=zh_CN";
    String GET_USERS_URL = "https://api.weixin.qq.com/cgi-bin/user/get?access_token={access_token}";
    String GET_NEXT_USERS_URL = "https://api.weixin.qq.com/cgi-bin/user/get?access_token={access_token}&next_openid={next_openid}";
    String SNS_API_USER_INFO = "https://api.weixin.qq.com/sns/userinfo?access_token={access_token}&openid={openid}&lang=zh_CN";
    String MOBILE_USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token={access_token}&openid={openid}&lang=zh_CN";
    String LIST_BLACKLIST_URL = "https://api.weixin.qq.com/cgi-bin/tags/members/getblacklist?access_token={access_token}";
    String BATCH_BALCKLIST_URL = "https://api.weixin.qq.com/cgi-bin/tags/members/batchblacklist?access_token={access_token}";
    String UNBATCH_BACKLIST_URL = "https://api.weixin.qq.com/cgi-bin/tags/members/batchunblacklist?access_token={access_token}";

    WeiXinResult.UserInfoObject storeWeiXinUserInfo(String openId, String accessToken, Profile.ProfileType profileType);

    WeiXinResult.UserInfoObject storeWeiXinUserInfoByMobileApp(String openId);

    Role getUserRole(Integer profileId);

    void updateMemberId(Integer profileId, String memberId);

    /**
     * 根据riseid获取用户的详细信息
     */
    Profile getProfileByRiseId(String riseId);

    /**
     * 根据profile id 列表获取用户的详细信息
     */
    List<Profile> getProfiles(List<Integer> profileIds);

    /**
     * 根据openid获取用户详情
     */
    Profile getProfile(String openid, boolean realTime);

    /**
     * 根据profileId获取用户详情
     */
    Profile getProfile(Integer profileId);

    /**
     * 根据profile id 列表获取用户的详细信息
     */
    List<Profile> getProfilesByMemberIds(List<String> memberIds);

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

    Pair<Boolean, String> hasPrivilegeForMiniMBA(Integer profileId);

    Pair<Boolean, String> hasPrivilegeForApply(Integer profileId, Integer project);

    Pair<Boolean, String> hasPrivilegeForBusinessSchool(Integer profileId);

    /**
     * 获取黑名单列表
     * (该接口一次最多返回10000条数据)
     */
    List<String> loadBlackListOpenIds();

    /**
     * 批量拉黑用户
     *
     * @param openidList 拉黑用户列表
     */
    boolean batchBlackList(List<String> openidList);

    /**
     * 取消拉黑用户
     *
     * @param openidList 取消拉黑用户列表
     */
    boolean batchUnBlackList(List<String> openidList);

    Integer loadUserScheduleCategory(Integer profileId);

    Profile getProfileByUnionId(String unionId);

    WeiXinResult.UserInfoObject getProfileFromWeiXinByUnionId(String unionId);

    /**
     * 获得最新的学号
     */
    RiseClassMember getLatestMemberId(Integer profileId);

    /**
     * 获取当前有效的RiseMember
     */
    @Deprecated
    RiseMember getCurrentRiseMember(Integer profileId);

    /**
     * 根据班级和小组进行查询（分页）
     */
    List<RiseClassMember> getByClassNameGroupId(Page page, String className, String groupId);

    /**
     * 根据班级名进行查询（分页）
     */
    List<RiseClassMember> getByClassName(Page page, String className);

    Pair<Integer, String> addVipRiseMember(String riseId, String memo, Integer monthLength);

    /**
     * 获取用户最后一次审批通过的商学院申请的通过时间
     */
    Date loadLastApplicationDealTime(Integer profileId, Integer project);


    boolean hasAvailableApply(Integer profileId, Integer project);


    boolean hasAvailableApply(List<BusinessSchoolApplication> applyList, Integer project);

    Pair<Boolean, String> hasPrivilegeForCamp(Integer profileId);
}
