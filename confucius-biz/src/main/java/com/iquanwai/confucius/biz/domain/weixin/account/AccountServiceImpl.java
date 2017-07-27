package com.iquanwai.confucius.biz.domain.weixin.account;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.iquanwai.confucius.biz.dao.RedisUtil;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.common.customer.PromotionUserDao;
import com.iquanwai.confucius.biz.dao.common.permission.UserRoleDao;
import com.iquanwai.confucius.biz.dao.wx.FollowUserDao;
import com.iquanwai.confucius.biz.dao.wx.RegionDao;
import com.iquanwai.confucius.biz.exception.NotFollowingException;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.Region;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private RegionDao regionDao;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private RedisUtil redisUtil;

    private List<Region> provinceList;

    private List<Region> cityList;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private PromotionUserDao promotionUserDao;

    private Map<String, Integer> userRoleMap = Maps.newHashMap();

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        loadAllProvinces();
        loadCities();
        loadUserRole();
    }

    private void loadUserRole() {
        List<UserRole> userRoleList = userRoleDao.loadAll(UserRole.class);

        userRoleList.stream().filter(userRole1 -> !userRole1.getDel()).forEach(userRole -> {
            userRoleMap.put(userRole.getOpenid(), userRole.getRoleId());
        });

        logger.info("role init complete");
    }

    public Account getAccount(String openid, boolean realTime) throws NotFollowingException {
        if (realTime) {
            return getAccountFromWeixin(openid);
        } else {
            //先从数据库查询account对象
            Account account = followUserDao.queryByOpenid(openid);
            if (account != null) {
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
    public Profile getProfile(Integer profileId) {
        Profile profile = profileDao.load(Profile.class, profileId);

        if (profile != null) {
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

        return profile;
    }

    private Account getAccountFromWeixin(String openid) throws NotFollowingException {
        //调用api查询account对象
        String url = USER_INFO_URL;
        Map<String, String> map = Maps.newHashMap();
        map.put("openid", openid);
        logger.info("请求用户信息:{}", openid);
        url = CommonUtils.placeholderReplace(url, map);

        String body = restfulHelper.get(url);
        logger.info("请求用户信息结果:{}", body);
        Map<String, Object> result = CommonUtils.jsonToMap(body);
        Account accountNew = new Account();
        try {
            ConvertUtils.register((aClass, value) -> {
                if (value == null)
                    return null;

                if (!(value instanceof Double)) {
                    logger.error("不是日期类型");
                    throw new ConversionException("不是日期类型");
                }
                Double time = (Double) value * 1000;

                return new DateTime(time.longValue()).toDate();
            }, Date.class);

            BeanUtils.populate(accountNew, result);
            if (accountNew.getSubscribe() == 0) {
                throw new NotFollowingException();
            }
            Account finalQuery = followUserDao.queryByOpenid(openid);
            if (finalQuery == null) {
                redisUtil.lock("lock:wx:user:insert", (lock) -> {
                    if (accountNew.getNickname() != null) {
                        logger.info("插入用户信息:{}", accountNew);
                        followUserDao.insert(accountNew);
                        // 插入profile表
                        Profile profile = getProfileFromDB(accountNew.getOpenid());
                        if (profile == null) {
                            profile = new Profile();
                            try {
                                BeanUtils.copyProperties(profile, accountNew);
                                logger.info("插入Profile表信息:{}", profile);
                                profile.setRiseId(CommonUtils.randomString(7));
                                profileDao.insertProfile(profile);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                logger.error("beanUtils copy props error", e);
                            } catch (SQLException err) {
                                profile.setRiseId(CommonUtils.randomString(7));
                                try {
                                    profileDao.insertProfile(profile);
                                } catch (SQLException subErr) {
                                    logger.error("插入Profile失败，openId:{},riseId:{}", profile.getOpenid(), profile.getRiseId());
                                }
                            }
                        }
                    }
                });
            } else {
                logger.info("更新用户信息:{}", accountNew);
                if (accountNew.getNickname() != null) {
                    followUserDao.updateMeta(accountNew);
                }
            }
        } catch (NotFollowingException e1) {
            throw new NotFollowingException();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return accountNew;
    }

    public void collectUsers() {
        //调用api查询account对象
        String url = GET_USERS_URL;

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

    public void collectNewUsers() {
        //调用api查询account对象
        String url = GET_USERS_URL;

        String body = restfulHelper.get(url);

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
    public List<Region> loadAllProvinces() {
        if (provinceList == null) {
            provinceList = regionDao.loadAllProvinces();
        }
        return provinceList;
    }

    @Override
    public List<Region> loadCities() {
        if (cityList == null) {
            cityList = regionDao.loadAllCities();
        }
        return cityList;
    }

    @Override
    public Region loadProvinceByName(String name) {
        Region result = null;
        if (provinceList != null) {
            for (Region province : provinceList) {
                if (StringUtils.equals(province.getName(), name)) {
                    result = province;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public Region loadCityByName(String name) {
        Region result = null;
        if (cityList != null) {
            for (Region city : cityList) {
                if (StringUtils.equals(city.getName(), name)) {
                    result = city;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public void unfollow(String openid) {
        followUserDao.unsubscribe(openid);
    }

    @Override
    public void updateRiseMember(String openid, Integer riseMember) {
        if(riseMember == Constants.RISE_MEMBER.COURSE_USER){
            Profile profile = profileDao.queryByOpenId(openid);
            //会员的等级最高
            if(profile.getRiseMember() == Constants.RISE_MEMBER.MEMBERSHIP){
                return;
            }
        }
        profileDao.updateRiseMember(openid, riseMember);
    }

    private Profile getProfileFromDB(String openid) {
        Profile profile = profileDao.queryByOpenId(openid);

        if (profile != null) {
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

        return profile;
    }

}
