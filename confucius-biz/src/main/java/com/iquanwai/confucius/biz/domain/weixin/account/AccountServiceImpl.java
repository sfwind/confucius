package com.iquanwai.confucius.biz.domain.weixin.account;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.iquanwai.confucius.biz.dao.RedisUtil;
import com.iquanwai.confucius.biz.dao.common.customer.CustomerStatusDao;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.common.permission.UserRoleDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseCertificateDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseClassMemberDao;
import com.iquanwai.confucius.biz.dao.wx.CallbackDao;
import com.iquanwai.confucius.biz.dao.wx.FollowUserDao;
import com.iquanwai.confucius.biz.exception.NotFollowingException;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.po.common.customer.CustomerStatus;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.po.fragmentation.CourseScheduleDefault;
import com.iquanwai.confucius.biz.po.fragmentation.RiseCertificate;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.joda.time.DateTime;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.*;

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

    private Map<String, Integer> userRoleMap = Maps.newHashMap();

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final int WX_BLACKLIST_DEFAULT_PAGE_SIZE = 10000;

    @PostConstruct
    public void init() {
        loadUserRole();
    }

    private void loadUserRole() {
        List<UserRole> userRoleList = userRoleDao.loadAll(UserRole.class);

        userRoleList.stream().filter(userRole1 -> !userRole1.getDel()).forEach(userRole -> userRoleMap.put(userRole.getOpenid(), userRole.getRoleId()));

        logger.info("role init complete");
    }

    @Override
    public Account getAccount(String openid, boolean realTime) throws NotFollowingException {
        if (realTime) {
            return getAccountFromWeixin(openid);
        } else {
            //先从数据库查询account对象
            Account account = followUserDao.queryByOpenid(openid);
            if (account != null) {
                if (account.getSubscribe() == 0) {
                    // 曾经关注，现在取关的人
                    throw new NotFollowingException();
                }
                return account;
            }
            //从微信处获取
            return getAccountFromWeixin(openid);
        }
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
            profile.setRiseMember(getRiseMember(profile.getId()));
            if (profile.getHeadimgurl() != null) {
                profile.setHeadimgurl(profile.getHeadimgurl().replace("http:", "https:"));
            }
            Integer role = userRoleMap.get(profile.getOpenid());
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
    public Profile getProfileByRiseId(String riseId) {
        Profile profile = profileDao.queryByRiseId(riseId);
        if (profile != null) {
            profile.setRiseMember(getRiseMember(profile.getId()));
        }
        return profile;
    }

    @Override
    public List<Profile> getProfiles(List<Integer> profileIds) {
        List<Profile> profiles = profileDao.queryAccounts(profileIds);
        profiles.forEach(profile -> {
            profile.setRiseMember(getRiseMember(profile.getId()));
            if (profile.getHeadimgurl() != null) {
                profile.setHeadimgurl(profile.getHeadimgurl().replace("http:", "https:"));
            }
            Integer role = userRoleMap.get(profile.getOpenid());
            if (role == null) {
                profile.setRole(0);
            } else {
                profile.setRole(role);
            }
            // TODO 处理头像问题
        });
        return profiles;
    }

    private Integer getRiseMember(Integer profileId) {
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        if (riseMember == null) {
            return 0;
        }
        Integer memberTypeId = riseMember.getMemberTypeId();
        if (memberTypeId == null) {
            return 0;
        }
        // 精英或者专业版用户
        if (memberTypeId == RiseMember.HALF || memberTypeId == RiseMember.ANNUAL || memberTypeId == RiseMember.ELITE || memberTypeId == RiseMember.HALF_ELITE) {
            return 1;
        } else if (memberTypeId == RiseMember.CAMP) {
            return 3;
        } else if (memberTypeId == RiseMember.COURSE) {
            return 2;
        } else {
            return 0;
        }
    }

    private Account getAccountFromWeixin(String openid) throws NotFollowingException {
        // 调用api查询用户详情
        String url = USER_INFO_URL;
        Map<String, String> map = Maps.newHashMap();
        map.put("openid", openid);
        logger.info("请求用户信息:{}", openid);
        url = CommonUtils.placeholderReplace(url, map);
        String body = restfulHelper.get(url);
        logger.info("请求用户信息结果:{}", body);
        Map<String, Object> result = CommonUtils.jsonToMap(body);

        Account account = new Account();
        try {
            // TODO
            ConvertUtils.register((aClass, value) -> {
                if (value == null) {
                    return null;
                }
                if (!(value instanceof Double)) {
                    logger.error("不是日期类型");
                    throw new ConversionException("不是日期类型");
                }
                Double time = (Double) value * 1000;
                return new DateTime(time.longValue()).toDate();
            }, Date.class);
            BeanUtils.populate(account, result);
            if (account.getSubscribe() != null && account.getSubscribe() == 0) {
                //未关注直接抛异常
                throw new NotFollowingException();
            }

            redisUtil.lock("lock:wx:user:insert", (lock) -> {
                Account existAccount = followUserDao.queryByOpenid(openid);
                if (existAccount == null) {
                    logger.info("插入用户信息:{}", account);
                    followUserDao.insert(account);
                    // 插入profile表
                    Profile profile = getProfileFromDB(account.getOpenid());
                    if (profile == null) {
                        ModelMapper modelMapper = new ModelMapper();
                        profile = modelMapper.map(account, Profile.class);
                        try {
                            logger.info("插入Profile表信息:{}", profile);
                            profile.setRiseId(CommonUtils.randomString(7));
                            profileDao.insertProfile(profile);
                        } catch (SQLException err) {
                            profile.setRiseId(CommonUtils.randomString(7));
                            try {
                                profileDao.insertProfile(profile);
                            } catch (SQLException subErr) {
                                logger.error("插入Profile失败，openId:{},riseId:{}", profile.getOpenid(), profile.getRiseId());
                            }
                        }
                    } else {
                        ModelMapper modelMapper = new ModelMapper();
                        profile = modelMapper.map(account, Profile.class);
                        profileDao.updateOAuthFields(profile);
                    }
                } else {
                    followUserDao.updateOAuthFields(account);
                }
            });
        } catch (NotFollowingException e1) {
            throw new NotFollowingException();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return account;
    }

    @Override
    public String getRealHeadImgUrlFromWeixin(String openId) throws NotFollowingException {
        // 调用api查询account对象
        String url = USER_INFO_URL;
        Map<String, String> map = Maps.newHashMap();
        map.put("openid", openId);
        logger.info("请求用户信息:{}", openId);
        url = CommonUtils.placeholderReplace(url, map);
        String body = restfulHelper.get(url);
        logger.info("请求用户信息结果:{}", body);
        Map<String, Object> result = CommonUtils.jsonToMap(body);
        Account accountNew = new Account();
        try {
            ConvertUtils.register((aClass, value) -> {
                if (value == null) {
                    return null;
                }
                if (!(value instanceof Double)) {
                    logger.error("不是日期类型");
                    throw new ConversionException("不是日期类型");
                }
                Double time = (Double) value * 1000;
                return new DateTime(time.longValue()).toDate();
            }, Date.class);

            BeanUtils.populate(accountNew, result);
            if (accountNew.getSubscribe() != null && accountNew.getSubscribe() == 0) {
                //未关注直接抛异常
                throw new NotFollowingException();
            } else {
                return accountNew.getHeadimgurl();
            }
        } catch (NotFollowingException e1) {
            throw new NotFollowingException();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void collectUsers() {
        //调用api查询account对象

        String body = restfulHelper.get(GET_USERS_URL);

        UsersDto usersDto = new Gson().fromJson(body, UsersDto.class);
        String lastOpenid = "";
        for (String openid : usersDto.getData().getOpenid()) {
            lastOpenid = openid;
            try {
                getAccount(openid, true);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.info("最后一个openid:" + lastOpenid);
        logger.info("处理完成");
    }

    @Override
    public void collectNewUsers() {
        //调用api查询account对象

        String body = restfulHelper.get(GET_USERS_URL);

        UsersDto usersDto = new Gson().fromJson(body, UsersDto.class);

        List<String> openids = followUserDao.queryAll();
        for (String openid : usersDto.getData().getOpenid()) {
            if (!openids.contains(openid)) {
                try {
                    getAccountFromWeixin(openid);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        logger.info("处理完成");
    }

    @Override
    public void collectNext(String nextOpenid) {
        //调用api查询account对象
        String url = GET_NEXT_USERS_URL;
        url = url.replace("{next_openid}", nextOpenid);
        String body = restfulHelper.get(url);

        UsersDto usersDto = new Gson().fromJson(body, UsersDto.class);
        String lastOpenid = "";
        for (String openid : usersDto.getData().getOpenid()) {
            lastOpenid = openid;
            try {
                getAccount(openid, true);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.info("最后一个openid:" + lastOpenid);
        logger.info("处理完成");
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
        RiseClassMember riseClassMember = riseClassMemberDao.queryValidClassMemberByMemberId(memberId);
        if (riseClassMember != null && riseClassMember.getProfileId() != null) {
            return getProfile(riseClassMember.getProfileId());
        } else {
            return null;
        }
    }

    private Profile getProfileFromDB(String openid) {
        Profile profile = profileDao.queryByOpenId(openid);
        if (profile != null) {
            profile.setRiseMember(getRiseMember(profile.getId()));
            if (profile.getHeadimgurl() != null) {
                profile.setHeadimgurl(profile.getHeadimgurl().replace("http:", "https:"));
            }
            Integer role = userRoleMap.get(profile.getOpenid());
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
    public Boolean hasPrivilegeForBusinessSchool(Integer profileId) {
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        Boolean result = false;

        if (riseMember != null) {
            Integer memberTypeId = riseMember.getMemberTypeId();
            //如果用户是专业版或者精英版,则无需申请
            if (RiseMember.HALF == memberTypeId || RiseMember.ANNUAL == memberTypeId || RiseMember.ELITE == memberTypeId || RiseMember.HALF_ELITE == memberTypeId) {
                result = true;
            }
        }

        //如果用户曾经获得证书,则无需申请
        if (!result) {
            RiseCertificate riseCertificate = riseCertificateDao.loadGraduateByProfileId(profileId);
            result = riseCertificate != null;
        }

        //如果用户已经通过申请,则无需再次申请
        if (!result) {
            result = customerStatusDao.load(profileId, CustomerStatus.APPLY_BUSINESS_SCHOOL_SUCCESS) != null;
        }

        return result;
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
        // 老用户
        CustomerStatus status = customerStatusDao.load(profileId, CustomerStatus.SCHEDULE_LESS);
        if (status != null) {
            return CourseScheduleDefault.CategoryType.OLD_STUDENT;
        } else {
            return CourseScheduleDefault.CategoryType.NEW_STUDENT;
        }
    }

    @Override
    public Profile queryByUnionId(String unionId) {
        return profileDao.queryByUnionId(unionId);
    }

    @Override
    public int updateHeadImageUrl(Integer profileId, String headImgUrl) {
        return profileDao.updateHeadImgUrl(profileId, headImgUrl);
    }

    @Override
    public int initProfileAndFollowUser(String unionId, String nickName, String avatarUrl, Integer gender) {
        int result = 1;

        Account account = followUserDao.queryByUnionId(unionId);
        if (account == null) {
            Callback callback = callbackDao.queryByUnionId(unionId);
            String weMiniOpenId = callback.getWeMiniOpenid();

            account = new Account();
            account.setWeMiniOpenId(weMiniOpenId);
            account.setUnionid(unionId);
            account.setNickname(nickName);
            account.setSex(gender);
            account.setHeadimgurl(avatarUrl);
            result *= followUserDao.insert(account) > 0 ? 1 : -1;
        } else {
            Callback callback = callbackDao.queryByUnionId(unionId);
            String weMiniOpenId = callback.getWeMiniOpenid();
            account.setWeMiniOpenId(weMiniOpenId);
            result *= followUserDao.updateOAuthFields(account);
        }

        Profile profile = queryByUnionId(unionId);
        if (profile == null) {
            profile = new Profile();
            profile.setUnionid(unionId);
            profile.setNickname(nickName);
            profile.setHeadimgurl(avatarUrl);
            profile.setRiseId(CommonUtils.randomString(7));
            try {
                result *= profileDao.insertProfile(profile);
            } catch (SQLException e) {
                profile.setRiseId(CommonUtils.randomString(7));
                try {
                    result *= profileDao.insertProfile(profile) > 0 ? 1 : -1;
                } catch (SQLException e1) {
                    logger.error(e1.getLocalizedMessage(), e);
                }
            }
        }

        return result;
    }

}