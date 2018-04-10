package com.iquanwai.confucius.biz.domain.weixin.account;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.RedisUtil;
import com.iquanwai.confucius.biz.dao.apply.BusinessSchoolApplicationDao;
import com.iquanwai.confucius.biz.dao.common.customer.CustomerStatusDao;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.common.permission.UserRoleDao;
import com.iquanwai.confucius.biz.dao.fragmentation.CourseScheduleDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseCertificateDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseClassMemberDao;
import com.iquanwai.confucius.biz.dao.wx.CallbackDao;
import com.iquanwai.confucius.biz.dao.wx.FollowUserDao;
import com.iquanwai.confucius.biz.domain.course.signup.RiseMemberManager;
import com.iquanwai.confucius.biz.domain.fragmentation.CacheService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
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
import com.iquanwai.confucius.biz.po.fragmentation.BusinessSchoolConfig;
import com.iquanwai.confucius.biz.po.fragmentation.CourseScheduleDefault;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampConfig;
import com.iquanwai.confucius.biz.po.fragmentation.RiseCertificate;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import com.iquanwai.confucius.biz.util.ThreadPool;
import com.iquanwai.confucius.biz.util.page.Page;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private RiseCertificateDao riseCertificateDao;
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
                try {
                    profileDao.insertProfile(profile);
                } catch (SQLException e) {
                    profile.setRiseId(CommonUtils.randomString(7));
                    try {
                        profileDao.insertProfile(profile);
                    } catch (SQLException e1) {
                        logger.error(e1.getLocalizedMessage(), e);
                    }
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
    public Pair<Boolean, String> hasPrivilegeForMiniMBA(Integer profileId) {
        List<BusinessSchoolApplication> applyList = businessSchoolApplicationDao.loadApplyList(profileId);

        if (this.hasAvailableApply(applyList, Constants.Project.CORE_PROJECT)) {
            return Pair.of(false, "您已经申请核心能力项目，可联系圈外更改申请项目");
        }
        if (this.hasAvailableApply(applyList, Constants.Project.BUSINESS_THOUGHT_PROJECT)) {
            return Pair.of(true, "ok");
        } else {
            // 查看是否有进行中的申请
            List<BusinessSchoolApplication> checking = applyList.stream().filter(item -> !item.getDeal()).collect(Collectors.toList());
            if (!checking.isEmpty()) {
                if (checking.stream().anyMatch(item -> item.getProject() == Constants.Project.CORE_PROJECT)) {
                    return Pair.of(false, "你已申请核心能力课，可申请调换");
                } else {
                    return Pair.of(false, "您的申请正在审核中");
                }
            } else {
                return Pair.of(false, "请先提交申请");
            }
        }
    }

    @Override
    public Pair<Boolean, String> hasPrivilegeForApply(Integer profileId, Integer project) {
        List<BusinessSchoolApplication> applyList = businessSchoolApplicationDao.loadApplyList(profileId);
        if (!this.hasAvailableApply(profileId, project)) {
            return Pair.of(false, "请先提交申请");
        } else {
            if (Constants.Project.CORE_PROJECT == project) {
                if (this.hasAvailableApply(profileId, Constants.Project.BUSINESS_THOUGHT_PROJECT)) {
                    return Pair.of(false, "您已有进阶课报名权限，可以联系圈外更改报名项目");
                }
            } else if (Constants.Project.BUSINESS_THOUGHT_PROJECT == project) {
                if (this.hasAvailableApply(profileId, Constants.Project.CORE_PROJECT)) {
                    return Pair.of(false, "您已有核心课报名权限，可以联系圈外更改报名项目");
                }
            }
            if (applyList.stream().anyMatch(item -> !item.getDeal())) {
                return Pair.of(false, "您的申请正在审核中");
            }
        }
        return Pair.of(true, "ok");
    }

    @Override
    public Pair<Boolean, String> hasPrivilegeForBusinessSchool(Integer profileId) {
        RiseMember riseMember = riseMemberManager.coreBusinessSchoolUser(profileId);
        Boolean result = false;

        if (riseMember != null) {
            Integer memberTypeId = riseMember.getMemberTypeId();
            //如果用户是专业版或者精英版,则无需申请
            // TODO 精英版不能续费
            if (RiseMember.HALF == memberTypeId || RiseMember.ANNUAL == memberTypeId || RiseMember.ELITE == memberTypeId || RiseMember.HALF_ELITE == memberTypeId) {
                result = true;
            }
        }

        List<BusinessSchoolApplication> applyList = businessSchoolApplicationDao.loadApplyList(profileId);
        if (this.hasAvailableApply(applyList, Constants.Project.BUSINESS_THOUGHT_PROJECT)) {
            return Pair.of(false, "您已经申请商业进阶课，可联系圈外更改申请项目");
        }

        if (this.hasAvailableApply(applyList, Constants.Project.CORE_PROJECT)) {
            return Pair.of(true, "ok");
        }


        //如果用户曾经获得证书,则无需申请
        if (!result) {
            RiseCertificate riseCertificate = riseCertificateDao.loadGraduateByProfileId(profileId);
            result = riseCertificate != null;
        }


        // 购买会员
        BusinessSchoolConfig businessSchoolConfig = cacheService.loadBusinessCollegeConfig(RiseMember.ELITE);
        if (!businessSchoolConfig.getPurchaseSwitch()) {
            return Pair.of(false, "商学院报名临时关闭\n记得及时关注开放时间哦");
        } else {
            // 查看是否开放报名
            if (ConfigUtils.getRisePayStopTime().before(new Date())) {
                return Pair.of(false, "谢谢您关注圈外商学院!\n本次报名已达到限额\n记得及时关注下期开放通知哦");
            }
        }

        if (result) {
            return Pair.of(true, "ok");
        } else {
            // 查看是否有进行中的申请
            List<BusinessSchoolApplication> checking = applyList.stream().filter(item -> !item.getDeal()).collect(Collectors.toList());
            if (!checking.isEmpty()) {
                if (checking.stream().anyMatch(item -> item.getProject() == Constants.Project.BUSINESS_THOUGHT_PROJECT)) {
                    return Pair.of(false, "你已申请商业进阶课，可申请调换");
                } else {
                    return Pair.of(false, "您的申请正在审核中");
                }
            } else {
                return Pair.of(false, "请先提交申请");
            }

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

    /**
     * 批量拉黑用户
     */
    @Override
    public boolean batchBlackList(List<String> openidList) {
        String url = BATCH_BALCKLIST_URL;

        String body = queryWXBlackListInterface(url, openidList);
        return checkIsSuccess(body);
    }

    /**
     * 批量取消拉黑用户
     */
    @Override
    public boolean batchUnBlackList(List<String> openidList) {
        String url = UNBATCH_BACKLIST_URL;

        String body = queryWXBlackListInterface(url, openidList);
        return checkIsSuccess(body);
    }

    private boolean checkIsSuccess(String body) {
        JSONObject resultJSON = JSON.parseObject(body);
        if ((Integer) resultJSON.get("errcode") == 0) {
            return true;
        } else {
            return false;
        }
    }

    private String queryWXBlackListInterface(String url, List<String> openidList) {
        //拼装JSON数据
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("openid_list", openidList);
        String body = restfulHelper.post(url, jsonObject.toJSONString());
        logger.info(body);
        return body;
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

        // TODO: 杨仁 增加project项目
        RiseMember currentRiseMember = riseMemberDao.loadValidRiseMember(profileId);
        if (currentRiseMember != null) {
            return new MutablePair<>(-1, "该用户已经是会员");
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
        return new MutablePair<>(result, null);
    }

    @Override
    public Date loadLastApplicationDealTime(Integer profileId, Integer project) {
        BusinessSchoolApplication businessSchoolApplication = businessSchoolApplicationDao.loadLastApproveApplication(profileId, project);
        if (businessSchoolApplication != null) {
            return businessSchoolApplication.getDealTime();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasAvailableApply(Integer profileId, Integer project) {
        return this.hasAvailableApply(businessSchoolApplicationDao.loadApplyList(profileId), project);
    }

    @Override
    public boolean hasAvailableApply(List<BusinessSchoolApplication> applyList, Integer project) {
        return applyList
                .stream()
                .filter(item -> Objects.equals(item.getProject(), project))
                .filter(item -> item.getStatus() == BusinessSchoolApplication.APPROVE)
                .filter(BusinessSchoolApplication::getDeal)
                .anyMatch(item -> DateUtils.intervalMinute(DateUtils.afterHours(item.getDealTime(), 24)) > 0);
    }

    @Override
    public Pair<Boolean, String> hasPrivilegeForCamp(Integer profileId) {
        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();
        RiseMember riseMember = riseMemberManager.coreBusinessSchoolUser(profileId);
        // 购买专项课
        if (riseMember != null && (RiseMember.HALF_ELITE == riseMember.getMemberTypeId() ||
                RiseMember.ELITE == riseMember.getMemberTypeId())) {
            return Pair.of(false, "您已经是圈外商学院学员，拥有主题专项课，无需重复报名\n如有疑问请在学习群咨询班长");
        } else {
            List<RiseClassMember> classMembers = riseClassMemberDao.queryByProfileId(profileId);
            List<Integer> months = classMembers.stream().map(RiseClassMember::getMonth).collect(Collectors.toList());
            if (months.contains(monthlyCampConfig.getSellingMonth())) {
                return Pair.of(false, "您已经是" + monthlyCampConfig.getSellingMonth() + "月专项课用户");
            }
            if (!monthlyCampConfig.getPurchaseSwitch()) {
                return Pair.of(false, "当月专项课已关闭报名");
            }
        }
        return Pair.of(true, "ok");
    }

}