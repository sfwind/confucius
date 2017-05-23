package com.iquanwai.confucius.biz.domain.weixin.account;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.iquanwai.confucius.biz.dao.RedisUtil;
import com.iquanwai.confucius.biz.dao.common.customer.EventWallDao;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.common.permission.UserRoleDao;
import com.iquanwai.confucius.biz.dao.wx.FollowUserDao;
import com.iquanwai.confucius.biz.dao.wx.RegionDao;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.EventWall;
import com.iquanwai.confucius.biz.po.Region;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
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
    private EventWallDao eventWallDao;

    private Map<String, Integer> userRoleMap = Maps.newHashMap();

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        loadAllProvinces();
        loadCities();
        loadUserRole();
    }

    private void loadUserRole(){
        List<UserRole> userRoleList = userRoleDao.loadAll(UserRole.class);

        userRoleList.stream().filter(userRole1 -> !userRole1.getDel()).forEach(userRole -> {
            userRoleMap.put(userRole.getOpenid(), userRole.getRoleId());
        });

        logger.info("role init complete");
    }

    public Account getAccount(String openid, boolean realTime) {
        //从数据库查询account对象
        Account account = followUserDao.queryByOpenid(openid);
        if(!realTime && account != null) {
            return account;
        }

        synchronized (this){
            // 这里再查询一遍，上面的代码老用户会走的，这里是只有新用户增加时才会走
            Account accountTemp = followUserDao.queryByOpenid(openid);
            if(!realTime && accountTemp != null) {
                return accountTemp;
            }
            account = getAccountFromWeixin(openid, accountTemp);
        }
        logger.info("返回用户信息:{}",account);
        return account;
    }

    private Account getAccountFromWeixin(String openid, Account account) {
        //调用api查询account对象
        String url = USER_INFO_URL;
        Map<String, String> map = Maps.newHashMap();
        map.put("openid", openid);
        logger.info("请求用户信息:{}",openid);
        url = CommonUtils.placeholderReplace(url, map);

        String body = restfulHelper.get(url);
        logger.info("请求用户信息结果:{}",body);
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
            if(account==null) {
                redisUtil.lock("lock:wx:user:insert",(lock)->{
                    if(accountNew.getNickname()!=null){
                        Account finalQuery = followUserDao.queryByOpenid(openid);
                        if (finalQuery != null) {
                            // 已经插入了
                            return;
                        }
                        logger.info("插入用户信息:{}",accountNew);
                        followUserDao.insert(accountNew);
                        try {
                            updateProfile(accountNew);
                        } catch (Exception e) {
                            logger.error(e.getLocalizedMessage(), e);
                        }
                    }else{
                        //用户未关注
                        accountNew.setSubscribe(0);
                    }
                });
            }else{
                logger.info("更新用户信息:{}",accountNew);
                if(accountNew.getNickname()!=null) {
                    if(accountNew.getSubscribe()==null){
                        //未关注用户
                        accountNew.setSubscribe(0);
                    }else{
                        if(accountNew.getSubscribe()!=0){
                            //只更新关注用户
                            followUserDao.updateMeta(accountNew);
                            updateProfile(accountNew);
                        }
                    }
                }else {
                    //未关注用户
                    accountNew.setSubscribe(0);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return accountNew;
    }

    private void updateProfile(Account accountNew) throws IllegalAccessException, InvocationTargetException {
        Profile profile = getProfileFromDB(accountNew.getOpenid());
        if(profile==null){
            profile = new Profile();
            try{
                BeanUtils.copyProperties(profile, accountNew);
                logger.info("插入Profile表信息:{}",profile);
                profile.setRiseId(CommonUtils.randomString(7));
                profileDao.insertProfile(profile);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error("beanUtils copy props error",e);
            } catch (SQLException err){
                profile.setRiseId(CommonUtils.randomString(7));
                try{
                    profileDao.insertProfile(profile);
                } catch (SQLException subErr){
                    logger.error("插入Profile失败，openId:{},riseId:{}",profile.getOpenid(),profile.getRiseId());
                }
            }
        }else{
            //更新原数据
            BeanUtils.copyProperties(profile,accountNew);
            profileDao.updateMeta(profile);
        }
    }

    public void submitPersonalInfo(Account account) {
        followUserDao.updateInfo(account);
    }

    public void collectUsers() {
        //调用api查询account对象
        String url = GET_USERS_URL;

        String body = restfulHelper.get(url);

        UsersDto usersDto = new Gson().fromJson(body, UsersDto.class);

        for(String openid:usersDto.getData().getOpenid()) {
            getAccount(openid, true);
        }
        logger.info("处理完成");
    }

    public void collectNewUsers() {
        //调用api查询account对象
        String url = GET_USERS_URL;

        String body = restfulHelper.get(url);

        UsersDto usersDto = new Gson().fromJson(body, UsersDto.class);

        List<String> openids = followUserDao.queryAll();
        for(String openid:usersDto.getData().getOpenid()) {
            if(!openids.contains(openid)) {
                getAccountFromWeixin(openid, null);
            }
        }
        logger.info("处理完成");
    }

    @Override
    public List<Region> loadAllProvinces() {
        if(provinceList ==null){
            provinceList = regionDao.loadAllProvinces();
        }
        return provinceList;
    }

    @Override
    public List<Region> loadCities() {
        if(cityList==null) {
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
    public Profile getProfile(String openid, boolean realTime){
        Profile profile = getProfileFromDB(openid);
        if(!realTime && profile != null){
            return profile;
        }
        synchronized (this){
            Profile profileTemp = getProfileFromDB(openid);
            if(!realTime && profileTemp != null){
                return profileTemp;
            }
            Account account = followUserDao.queryByOpenid(openid);
            getAccountFromWeixin(openid,account);
            return getProfileFromDB(openid);
        }
    }


    @Override
    public List<EventWall> getEventWall() {
        List<EventWall> eventWalls = eventWallDao.loadAll(EventWall.class).stream().filter(item -> !item.getDel()).collect(Collectors.toList());
        eventWalls.forEach(item->{
            Date startTime = item.getStartTime();
            Date endTime = item.getEndTime();
            item.setStartStr(DateUtils.parseDateToFormat6(startTime));

            if (DateUtils.isSameDate(startTime, endTime)) {
                item.setEndStr(DateUtils.parseDateToTimeFormat(endTime));
            } else {
                item.setEndStr(DateUtils.parseDateToFormat6(endTime));
            }
        });
        eventWalls.sort((o1, o2) -> {
            if (o1.getAddTime() == null) {
                return 1;
            } else if (o2.getAddTime() == null) {
                return -1;
            }
            return o2.getAddTime().before(o1.getAddTime()) ? 1 : -1;
        });
        return eventWalls;
    }

    private Profile getProfileFromDB(String openid) {
        Profile profile = profileDao.queryByOpenId(openid);

        if(profile!=null) {
            if(profile.getHeadimgurl()!=null){
                profile.setHeadimgurl(profile.getHeadimgurl().replace("http:","https:"));
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
