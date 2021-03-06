package com.iquanwai.confucius.biz.domain.weixin.account;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.RedisUtil;
import com.iquanwai.confucius.biz.dao.apply.BusinessSchoolApplicationDao;
import com.iquanwai.confucius.biz.dao.common.customer.CustomerStatusDao;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.common.permission.UserRoleDao;
import com.iquanwai.confucius.biz.dao.fragmentation.CourseScheduleDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseClassMemberDao;
import com.iquanwai.confucius.biz.dao.wx.CallbackDao;
import com.iquanwai.confucius.biz.dao.wx.FollowUserDao;
import com.iquanwai.confucius.biz.domain.course.signup.MemberTypeManager;
import com.iquanwai.confucius.biz.domain.course.signup.RiseMemberManager;
import com.iquanwai.confucius.biz.domain.fragmentation.CacheService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.permission.PermissionService;
import com.iquanwai.confucius.biz.domain.weixin.api.WeiXinApiService;
import com.iquanwai.confucius.biz.domain.weixin.api.WeiXinResult;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.po.CourseSchedule;
import com.iquanwai.confucius.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.confucius.biz.po.common.customer.CustomerStatus;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.Role;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.po.fragmentation.course.CourseConfig;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import com.iquanwai.confucius.biz.util.ThreadPool;
import com.iquanwai.confucius.biz.util.page.Page;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/8/10.
 */
@Service
public class AccountServiceImpl implements AccountService {
    @Autowired
    public RestfulHelper restfulHelper;
    @Autowired
    private FollowUserDao followUserDao;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private CustomerStatusDao customerStatusDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private RiseClassMemberDao riseClassMemberDao;
    @Autowired
    private CallbackDao callbackDao;
    @Autowired
    private WeiXinApiService weiXinApiService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private PlanService planService;
    @Autowired
    private CourseScheduleDao courseScheduleDao;
    @Autowired
    private SensorsAnalytics sa;
    @Autowired
    private BusinessSchoolApplicationDao businessSchoolApplicationDao;
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private MemberTypeManager memberTypeManger;
    @Autowired
    private OperationLogService operationLogService;

    private Map<Integer, Integer> userRoleMap = Maps.newHashMap();

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final int WX_BLACKLIST_DEFAULT_PAGE_SIZE = 10000;

    @PostConstruct
    public void init() {
        loadUserRole();
    }

    private void loadUserRole() {
        List<UserRole> userRoleList = userRoleDao.loadAll(UserRole.class);
        userRoleList.stream().filter(userRole1 -> !userRole1.getDel()).forEach(userRole ->
                userRoleMap.put(userRole.getProfileId(), userRole.getRoleId()));
        logger.info("role init complete");
    }

    @Override
    public WeiXinResult.UserInfoObject storeWeiXinUserInfo(String openId, String accessToken, Profile.ProfileType profileType) {
        // TODO 优化，不能每次过来都调用微信接口，比较调用微信接口和查询 callback 的时间花费差异
        WeiXinResult.UserInfoObject userInfoObject = weiXinApiService.getWeiXinUserInfo(openId, accessToken);
        if (userInfoObject == null) {
            WeiXinResult.RefreshTokenObject refreshTokenObject = weiXinApiService.refreshWeiXinAccessToken(accessToken);
            if (refreshTokenObject == null || refreshTokenObject.getAccessToken() == null || refreshTokenObject.getRefreshToken() == null) {
                return null;
            } else {
                userInfoObject = storeWeiXinUserInfo(openId, refreshTokenObject.getAccessToken(), profileType);
            }
        }
        store(openId, userInfoObject, profileType);
        return userInfoObject;
    }

    @Override
    public WeiXinResult.UserInfoObject storeWeiXinUserInfoByMobileApp(String openId) {
        WeiXinResult.UserInfoObject userInfoObject = weiXinApiService.getWeiXinUserInfoByMobileApp(openId);
        if (userInfoObject != null) {
            store(openId, userInfoObject, Profile.ProfileType.MOBILE);
        }
        return userInfoObject;
    }

    @Override
    public void updateProfileByWeiXin(Profile profile) {
        WeiXinResult.UserInfoObject userInfoObject = weiXinApiService.getWeiXinUserInfoByMobileApp(profile.getOpenid());
        if(userInfoObject!=null){
            String headImgUrl = userInfoObject.getHeadImgUrl();
            profileDao.updateHeadImgUrl(profile.getId(),headImgUrl);
        }
    }

    private void store(String openId, WeiXinResult.UserInfoObject userInfoObject, Profile.ProfileType profileType) {
        String unionId = userInfoObject.getUnionId();
        String nickName = userInfoObject.getNickName();
        String headImgUrl = userInfoObject.getHeadImgUrl();
        Integer sex = userInfoObject.getSex();
        String country = userInfoObject.getCountry();
        String province = userInfoObject.getProvince();
        String city = userInfoObject.getCity();
        Integer subscribe = userInfoObject.getSubscribe();
        redisUtil.lock("lock:wx:user:insert:followUser", lock -> {
            Account account = followUserDao.queryByUnionId(unionId);
            if (account == null) {
                account = new Account();
                account.setUnionid(unionId);
                switch (profileType) {
                    case MOBILE:
                        account.setOpenid(openId);
                        account.setSubscribe_time(new Date());
                        if (subscribe != null) {
                            account.setSubscribe(subscribe);
                        } else {
                            account.setSubscribe(0);
                        }
                        break;
                    case PC:
                        account.setSubscribe(0);
                        break;
                    case MINI:
                        account.setSubscribe(0);
                        account.setWeMiniOpenId(openId);
                        break;
                    default:
                        break;
                }
                account.setNickname(nickName);
                account.setHeadimgurl(headImgUrl);
                account.setSex(sex);
                account.setCountry(country);
                account.setProvince(province);
                account.setCity(city);
                followUserDao.insert(account);
            } else {
                switch (profileType) {
                    case MOBILE:
                        account.setOpenid(openId);
                        account.setSubscribe_time(new Date());
                        if (subscribe != null) {
                            account.setSubscribe(subscribe);
                        }
                        break;
                    case PC:
                        break;
                    case MINI:
                        account.setWeMiniOpenId(openId);
                        break;
                    default:
                        break;
                }
                account.setNickname(nickName);
                account.setHeadimgurl(headImgUrl);
                account.setSex(sex);
                account.setCountry(country);
                account.setProvince(province);
                account.setCity(city);
                followUserDao.updateOAuthFields(account);
            }
        });
        redisUtil.lock("lock:wx:user:insert:profile", lock -> {
            Profile profile = profileDao.queryByUnionId(unionId);
            if (profile == null) {
                profile = new Profile();
                profile.setUnionid(unionId);
                switch (profileType) {
                    case MOBILE:
                        profile.setOpenid(openId);
                        break;
                    case PC:
                        break;
                    case MINI:
                        // TODO profile 增加 miniOpenId
                        break;
                    default:
                        break;
                }
                profile.setNickname(nickName);
                profile.setHeadimgurl(headImgUrl);
                profile.setRiseId(CommonUtils.randomString(7));
                Integer profileId = null;
                try {
                    profileId = profileDao.insertProfile(profile);
                } catch (SQLException e) {
                    profile.setRiseId(CommonUtils.randomString(7));
                    try {
                        profileId = profileDao.insertProfile(profile);
                    } catch (SQLException e1) {
                        logger.error(e1.getLocalizedMessage(), e);
                    }
                }
                if (profileId != null) {
                    operationLogService.profileSet(profileId, "openId", profile.getOpenid());
                }
            } else {
                switch (profileType) {
                    case MOBILE:
                        profile.setOpenid(openId);
                        break;
                    case PC:
                        break;
                    case MINI:
                        break;
                    default:
                        break;
                }
                operationLogService.profileSet(profile.getId(), "openId", profile.getOpenid());
                profileDao.updateOAuthFields(profile);
            }
        });
        ThreadPool.execute(() -> {
            Profile profile = profileDao.queryByUnionId(unionId);
            try {
                sa.profileSet(profile.getRiseId(), true, "nickname", profile.getNickname());
            } catch (InvalidArgumentException e) {
                logger.error("更新神策profile失败{}", profile);
            }
        });
    }


    /**
     * 获取用户角色信息
     */
    @Override
    public Role getUserRole(Integer profileId) {
        Role role = permissionService.getRole(profileId);
        if (role == null) {
            List<ImprovementPlan> plans = planService.loadUserPlans(profileId);
            List<RiseMember> allMember = riseMemberDao.loadPersonalAll(profileId);
            role = plans.isEmpty() && allMember.isEmpty() ? Role.stranger() : Role.student();
        }
        return role;
    }

    @Override
    public void updateMemberId(Integer profileId, String memberId) {
        profileDao.updateMemberId(profileId, memberId);
    }

    @Override
    public Profile getProfile(String openid, boolean realTime) {
        return getProfileFromDB(openid);
    }

    @Override
    public Profile getProfile(String openid) {
        return getProfileFromDB(openid);
    }

    @Override
    public Profile getProfile(Integer profileId) {
        Profile profile = profileDao.load(Profile.class, profileId);
        if (profile != null) {
            if (profile.getHeadimgurl() != null) {
                profile.setHeadimgurl(profile.getHeadimgurl().replace("http:", "https:"));
            }
            Integer role = userRoleMap.get(profile.getId());
            if (role == null) {
                profile.setRole(0);
            } else {
                profile.setRole(role);
            }
        }
        // TODO 处理头像问题
        return profile;
    }

    @Override
    public List<Profile> getProfilesByMemberIds(List<String> memberIds) {
        return profileDao.queryByMemberIds(memberIds);
    }

    @Override
    public Profile getProfileByRiseId(String riseId) {
        return profileDao.queryByRiseId(riseId);
    }

    @Override
    public List<Profile> getProfiles(List<Integer> profileIds) {
        List<Profile> profiles = profileDao.queryAccounts(profileIds);
        profiles.forEach(profile -> {
            if (profile.getHeadimgurl() != null) {
                profile.setHeadimgurl(profile.getHeadimgurl().replace("http:", "https:"));
            }
            Integer role = userRoleMap.get(profile.getId());
            if (role == null) {
                profile.setRole(0);
            } else {
                profile.setRole(role);
            }
            // TODO 处理头像问题
        });
        return profiles;
    }

    @Override
    public void unfollow(String openid) {
        followUserDao.unsubscribe(openid);
    }

    @Override
    public List<Profile> loadProfilesByNickName(String nickName) {
        return profileDao.loadProfilesByNickName(nickName);
    }

    @Override
    public List<Profile> loadAllProfilesByNickName(String nickName) {
        return profileDao.loadAllProfilesByNickName(nickName);
    }

    @Override
    public Profile loadProfileByMemberId(String memberId) {
        return profileDao.queryByMemberId(memberId);
    }

    private Profile getProfileFromDB(String openid) {
        Profile profile = profileDao.queryByOpenId(openid);
        if (profile != null) {
            if (profile.getHeadimgurl() != null) {
                profile.setHeadimgurl(profile.getHeadimgurl().replace("http:", "https:"));
            }
            Integer role = userRoleMap.get(profile.getId());
            if (role == null) {
                profile.setRole(0);
            } else {
                profile.setRole(role);
            }
        }
        // TODO 处理头像问题
        return profile;
    }


    @Override
    public Pair<Boolean, String> hasPrivilegeForApply(Integer profileId, Integer memberTypeId) {
        /*
        pre.是否需要申请
        1.1已经是商学院用户-核心能力项 x
        1.2可以报名商业思维且为报名-核心能力项 x
        1.3已经能报了-核心能力项 x

        2.1已经是商业思维用户-商业性思维 x
        2.2可以报名核心能力项目-商业思维 x
        2.3已经可以报名商业思维-商业思维 x

        3.有进行中的申请 x
        4.最近一个月被拒绝过 x
         */

        // TODO 过期状态、付费状态回写，如果已经付费，则相当于已经付费
        // TODO 核心能力项目不能申请分拆项目(两个六个月)
        // 是否已经报名本状态
        MemberType memberType = memberTypeManger.memberType(memberTypeId);
        boolean entryThis = riseMemberDao.loadValidRiseMemberByMemberTypeId(profileId, Lists.newArrayList(memberTypeId)).stream().findAny().isPresent();
        if (entryThis) {
            return Pair.of(false, "您已经报名" + memberType.getDescription() + ",无需重复申请");
        }

        List<BusinessSchoolApplication> applyList = businessSchoolApplicationDao.loadApplyList(profileId);
        // 是否有权限报名
        if (this.hasAvailableApply(applyList, memberTypeId)) {
            return Pair.of(false, "您已经有" + memberType.getDescription() + "报名权限，无需重复申请");
        }
        // 可以报名其他项目
        if (this.hasAvailableOtherApply(applyList, memberTypeId)) {
            return Pair.of(false, "您有其他项目报名权限，需申请更换报名项目");
        }
        // 检查有没有进行中
        if (applyList.stream().anyMatch(item -> !item.getDeal())) {
            return Pair.of(false, "您的申请正在审核中");
        }

        // 一个月之内被拒绝过
        List<BusinessSchoolApplication> rejectLists = applyList
                .stream()
                .filter(item -> item.getMemberTypeId().equals(memberTypeId))
                .filter(item -> item.getStatus() == BusinessSchoolApplication.REJECT)
                .filter(item -> new DateTime(item.getDealTime()).withTimeAtStartOfDay().plusMonths(1).isAfter(new DateTime().withTimeAtStartOfDay()))
                .collect(Collectors.toList());
        if (rejectLists.size() > 0) {
            Integer maxWaitDays = rejectLists
                    .stream()
                    .map(item -> DateUtils.interval(new DateTime(item.getDealTime()).withTimeAtStartOfDay().plusMonths(1).toDate(), new DateTime().withTimeAtStartOfDay().toDate()))
                    .max((Comparator.comparingInt(o -> o)))
                    .orElse(0);
            return Pair.of(false, "还有 " + maxWaitDays + " 天才能再次申请哦");
        }

        return Pair.of(true, "ok");
    }


    @Override
    public Pair<Boolean, String> hasPrivilegeForMember(Integer profileId, Integer memberTypeId) {
        /*
        1.查看是否开放报名
        2.已经报过的不能报名
        3.专业版直接付费
        4.需要申请
         */
        // 查看是否开放报名 x
        CourseConfig courseConfig = cacheService.loadCourseConfig(memberTypeId);
        MemberType memberType = memberTypeManger.memberType(memberTypeId);
        if (!courseConfig.getPurchaseSwitch()) {
            return Pair.of(false, memberType.getDescription() + "报名临时关闭\n记得及时关注开放时间哦");
        }
        // 已经报过的不能报名
        RiseMember riseMember = riseMemberDao.loadValidRiseMemberByMemberTypeId(profileId, memberTypeId);
        if (riseMember != null) {
            return Pair.of(false, "您已经是" + memberType.getDescription() + "用户，无需购买");
        }

        if (riseMemberManager.proMember(profileId) != null) {
            // 专业版直接付费 to Del
            return Pair.of(true, "ok");
        }
        // 有其他申请通过
        List<BusinessSchoolApplication> applyList = businessSchoolApplicationDao.loadApplyList(profileId);
        if (this.hasAvailableOtherApply(applyList, memberTypeId)) {
            return Pair.of(false, "您有其他项目报名权限，需申请更换报名项目");
        }
        // 申请通过
        if (this.hasAvailableApply(applyList, memberTypeId)) {
            return Pair.of(true, "ok");
        }

        boolean hasApplying = applyList.stream().anyMatch(item -> !item.getDeal());
        if (hasApplying) {
            return Pair.of(false, "您的申请正在审核中");
        } else {
            return Pair.of(false, "请先提交申请");
        }
    }

    /**
     * 获取黑名单的列表(所有名单)
     */
    @Override
    public List<String> loadBlackListOpenIds() {
        String url = LIST_BLACKLIST_URL;
        int count = 0;
        List<String> blackList = new ArrayList<>();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("begin_openid", "");
        String body = restfulHelper.post(url, jsonObject.toJSONString());
        String data = JSON.parseObject(body).getString("data");
        //获取data中的openidList
        if (data != null) {
            JSONObject dataJSON = JSON.parseObject(data);
            String openidList = dataJSON.getString("openid");
            blackList.addAll(Arrays.asList(openidList.substring(1, openidList.length() - 1).split(",")));
            String nextOpenid = JSON.parseObject(body).getString("next_openid");

            int total = Integer.valueOf(JSON.parseObject(body).getString("total"));
            //取出所有的openid
            while ((total - 1) / WX_BLACKLIST_DEFAULT_PAGE_SIZE > count) {
                jsonObject = new JSONObject();
                jsonObject.put("begin_openid", nextOpenid);
                body = restfulHelper.post(url, jsonObject.toJSONString());
                data = JSON.parseObject(body).getString("data");

                dataJSON = JSON.parseObject(data);
                openidList = dataJSON.getString("openid");
                blackList.addAll(Arrays.asList(openidList.substring(1, openidList.length() - 1).split(",")));
                nextOpenid = JSON.parseObject(body).getString("next_openid");

                count++;
            }
        }
        return blackList;
    }

    @Override
    public Integer loadUserScheduleCategory(Integer profileId) {
        CourseSchedule courseSchedule = courseScheduleDao.loadOldestCourseSchedule(profileId);
        if (courseSchedule != null) {
            return courseSchedule.getCategory();
        }
        // 老用户
        CustomerStatus status = customerStatusDao.load(profileId, CustomerStatus.SCHEDULE_LESS);
        if (status != null) {
            return CourseScheduleDefault.CategoryType.OLD_STUDENT;
        } else {
            return CourseScheduleDefault.CategoryType.NEW_STUDENT;
        }
    }

    /**
     * 获取 Profile 对象，如果数据库不存在，会重新从微信获取一次，并且存储到数据库中
     */
    @Override
    public Profile getProfileByUnionId(String unionId) {
        Profile profile = profileDao.queryByUnionId(unionId);
        if (profile == null) {
            getProfileFromWeiXinByUnionId(unionId);
        }
        profile = profileDao.queryByUnionId(unionId);
        return profile;
    }

    @Override
    public WeiXinResult.UserInfoObject getProfileFromWeiXinByUnionId(String unionId) {
        Callback callback = callbackDao.queryByUnionId(unionId);
        if (callback.getOpenid() != null && callback.getAccessToken() != null) {
            // 曾经手机端登录过
            return storeWeiXinUserInfo(callback.getOpenid(), callback.getAccessToken(), Profile.ProfileType.MOBILE);
        } else if (callback.getPcOpenid() != null && callback.getPcAccessToken() != null) {
            // 曾经 pc 端登陆过
            return storeWeiXinUserInfo(callback.getPcOpenid(), callback.getPcAccessToken(), Profile.ProfileType.PC);
        } else if (callback.getWeMiniOpenid() != null && callback.getWeMiniAccessToken() != null) {
            // 曾经小程序登陆过
            return storeWeiXinUserInfo(callback.getWeMiniOpenid(), callback.getWeMiniAccessToken(), Profile.ProfileType.MINI);
        } else {
            return null;
        }
    }

    @Override
    public RiseClassMember getLatestMemberId(Integer profileId) {
        List<RiseClassMember> riseClassMembers = riseClassMemberDao.queryByProfileId(profileId);
        RiseClassMember riseClassMember = riseClassMembers.stream().max(Comparator.comparing(RiseClassMember::getId)).orElse(null);
        return riseClassMember;
    }

    @Override
    public RiseMember getCurrentRiseMember(Integer profileId) {
        return riseMemberDao.loadValidRiseMember(profileId);
    }

    @Override
    public List<RiseClassMember> getByClassName(Page page, String className) {
        List<RiseClassMember> riseClassMembers = riseClassMemberDao.getByClassName(className, page);
        page.setTotal(riseClassMemberDao.getCountByClass(className));

        return riseClassMembers;
    }

    @Override
    public List<RiseClassMember> getByClassNameGroupId(Page page, String className, String groupId) {
        List<RiseClassMember> riseClassMembers = riseClassMemberDao.getByClassNameGroupId(page, className, groupId);
        page.setTotal(riseClassMemberDao.getCountByClassNameGroupId(className, groupId));

        return riseClassMembers;
    }

    @Override
    public Pair<Integer, String> addVipRiseMember(String riseId, String memo, Integer monthLength) {
        Profile profile = getProfileByRiseId(riseId);
        int profileId = profile.getId();

        List<RiseMember> currentRiseMembers = riseMemberManager.member(profileId);
        if (CollectionUtils.isNotEmpty(currentRiseMembers)) {
            return Pair.of(-1, "该用户已经是会员");
        }
        RiseMember riseMember = new RiseMember();
        riseMember.setProfileId(profileId);
        riseMember.setOrderId("manual");
        riseMember.setMemberTypeId(RiseMember.ELITE);
        riseMember.setOpenDate(new Date());
        riseMember.setExpireDate(DateUtils.afterMonths(new Date(), monthLength));
        riseMember.setExpired(false);
        riseMember.setMemo(memo);
        riseMember.setVip(true);
        int result = riseMemberDao.insert(riseMember);
        return Pair.of(result, null);
    }

    @Override
    public BusinessSchoolApplication loadLastApply(Integer profileId, Integer memberTypeId) {
        return businessSchoolApplicationDao.loadLastApproveApplication(profileId, memberTypeId);
    }

    @Override
    public boolean hasAvailableApply(List<BusinessSchoolApplication> applyList, Integer memberTypeId) {
        if (memberTypeId == RiseMember.BUSINESS_THOUGHT) {
            // TODO 目前不需要申请，大家都有付费权限
            return true;
        }
        return applyList
                .stream()
                .filter(item -> Objects.equals(item.getMemberTypeId(), memberTypeId))
                .filter(item -> item.getStatus() == BusinessSchoolApplication.APPROVE)
                .filter(BusinessSchoolApplication::getDeal)
                .filter(item -> !item.getExpired())
                .peek(item -> {
                    if (DateUtils.intervalMinute(DateUtils.afterHours(item.getDealTime(), 24)) <= 0) {
                        // 已经过期
                        item.setExpired(true);
                        businessSchoolApplicationDao.expiredApply(item.getId());
                    }
                })
                .filter(item -> !item.getEntry())
                .anyMatch(item -> !item.getExpired());
    }

    @Override
    public boolean hasAvailableOtherApply(List<BusinessSchoolApplication> applyList, Integer memberTypeId) {
        return applyList
                .stream()
                .filter(item -> !Objects.equals(item.getMemberTypeId(), memberTypeId))
                .filter(item -> item.getStatus() == BusinessSchoolApplication.APPROVE)
                .filter(BusinessSchoolApplication::getDeal)
                .filter(item -> !item.getExpired())
                .peek(item -> {
                    if (DateUtils.intervalMinute(DateUtils.afterHours(item.getDealTime(), 24)) <= 0) {
                        // 已经过期
                        item.setExpired(true);
                        businessSchoolApplicationDao.expiredApply(item.getId());
                    }
                })
                .filter(item -> !item.getEntry())
                .anyMatch(item -> !item.getExpired());
    }

    @Override
    public Pair<Boolean, String> hasPrivilegeForCamp(Integer profileId) {
        /*
        1.已经是商学院用户 x
        2.已经报过这个月的 x
        3.本月报名已经关闭 x
         */
        CourseConfig monthlyCampConfig = cacheService.loadCourseConfig(RiseMember.CAMP);
        RiseMember riseMember = riseMemberManager.oldMember(profileId);
        // 购买专项课
        if (riseMember != null && RiseMember.isMember(riseMember.getMemberTypeId())) {
            return Pair.of(false, "您已经是圈外商学院学员，拥有主题专项课，无需重复报名\n如有疑问请在学习群咨询班长");
        } else {
            RiseMember campRiseMember = riseMemberDao.loadValidRiseMemberByMemberTypeId(profileId, RiseMember.CAMP);
            if (campRiseMember != null) {
                return Pair.of(false, "您已经是" + monthlyCampConfig.getSellingMonth() + "月专项课用户");
            }
            if (!monthlyCampConfig.getPurchaseSwitch()) {
                return Pair.of(false, "当月专项课已关闭报名");
            }
        }
        return Pair.of(true, "ok");
    }

    @Override
    public Account getAccountByUnionId(String unionId) {
        return followUserDao.queryByUnionId(unionId);
    }

}