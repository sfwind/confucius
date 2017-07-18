package com.iquanwai.confucius.web.pc;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.wx.CallbackDao;
import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.permission.PermissionService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.exception.NotFollowingException;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.Role;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.systematism.ClassMember;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.web.account.websocket.LoginEndpoint;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Map;

/**
 * Created by nethunder on 2017/5/5.
 */
@Service
public class LoginUserService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 缓存已经登录的用户
     */
    public static Map<String, SoftReference<PCLoginUser>> pcLoginUserMap = Maps.newHashMap();

    @Autowired
    private OAuthService oAuthService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private CourseProgressService courseProgressService;
    @Autowired
    private PlanService planService;
    @Autowired
    private CallbackDao callbackDao;

    /**
     * 登录，就是缓存起来
     * @param sessionId sessionId,这个sessionIds是三个点拼起来的
     * @param pcLoginUser 用户
     */
    public  void login(String sessionId, PCLoginUser pcLoginUser) {
        SoftReference<PCLoginUser> temp = new SoftReference<PCLoginUser>(pcLoginUser);
        pcLoginUserMap.put(sessionId, temp);
    }

    /**
     * 根据sessionId判断用户是否登录
     * @param sessionId SessionId
     * @return  是否登录
     */
    public  boolean isLogin(String sessionId){
        SoftReference<PCLoginUser> softReference = pcLoginUserMap.get(sessionId);
        if(softReference!=null){
            PCLoginUser pcLoginUser = softReference.get();
            if (pcLoginUser != null) {
                logger.info("act:{},已登录,user:{},nickName:{}", sessionId, pcLoginUser.getOpenId(), pcLoginUser.getWeixin() != null ? pcLoginUser.getWeixin().getWeixinName() : "没有微信信息");
                return true;
            } else {
                logger.info("act:{},softReference失效", sessionId);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 用户登出，删除登录缓存数据
     * @param sessionId 存储的 cookie 的值
     */
    public void logout(String sessionId) {
        pcLoginUserMap.remove(sessionId);
    }

    /**
     * 调用微信接口查看当前扫码用户是否已经关注
     * @return true: 用户已关注 false: 用户未关注
     */
    public boolean userIsFollowing(PCLoginUser loginUser) {
        Account account = null;
        try {
            //TODO:改造成数据库查询
            account = accountService.getAccount(loginUser.getOpenId(), true);
        } catch(NotFollowingException e) {
            if(loginUser.getWeixin() != null) {
                logger.info(loginUser.getWeixin().getWeixinName() + "未关注");
            }
        }
        return account != null;
    }

    /**
     * -1 key查不到callback，清除掉
     * -2 key查到了，但是获取不到user，应该是没点服务号
     * 1 成功
     */
    public Pair<Integer,Callback> refreshLogin(String sessionId){
        // 有key但是没有value，重新查一遍
        // 先检查这个cookie是否合法
        Callback callback = callbackDao.queryByPcAccessToken(sessionId);
        if (callback == null) {
            // 不合法
            return new MutablePair<>(-1, null);
        } else {
            // 合法，再查一遍
            Pair<Integer, PCLoginUser> result = getLoginUser(sessionId);
            if (result.getLeft() < 0) {
                logger.info("key:{} is lost , remove cookie", sessionId);
                pcLoginUserMap.remove(sessionId);
                return new MutablePair<>(-2, callback);
            } else {
                logger.info("key:{} is lost , search again: {}",sessionId, result.getRight());
                login(sessionId, result.getRight());
                return new MutablePair<>(1, callback);
            }
        }
    }
    public Boolean checkPermission(Integer roleId, String uri) {
        return permissionService.checkPermission(roleId, uri);
    }

    /**
     * 获取PCLoginUser
     *
     * @return -1:没有cookie <br/>
     * -2:accessToken无效<br/>
     * -3:没有关注<br/>
     * 1:PCLoginUser
     */
    public Pair<Integer, PCLoginUser> getLoginUser(HttpServletRequest request) {
        String pcToken = CookieUtils.getCookie(request, LoginEndpoint.QUANWAI_TOKEN_COOKIE_NAME);
        if (StringUtils.isEmpty(pcToken)) {
            return new MutablePair<>(-1, null);
        } else {
            return getLoginUser(pcToken);
        }
    }

    /**
     * 获取PCLoginUser
     *
     * @return -1:没有cookie <br/>
     * -2:accessToken无效,没有点页面<br/>
     * -3:没有关注，一般不会走到这个<br/>
     * -4:一般是没有关注服务号
     * 1:PCLoginUser
     */
    public Pair<Integer, PCLoginUser> getLoginUser(String accessToken) {
        // 先检查有没有缓存
        SoftReference<PCLoginUser> pcLoginUserSoftReference = pcLoginUserMap.get(accessToken);
        if (pcLoginUserSoftReference != null && pcLoginUserSoftReference.get() != null) {
            logger.debug("已缓存,_qt:{}", accessToken);
            return new MutablePair<>(1, pcLoginUserSoftReference.get());
        }

        String openid = oAuthService.pcOpenId(accessToken);
        if (openid == null) {
            // 没有查到openid，一般是该用户没有关注服务号
            logger.info("accessToken:{} can't find openid", accessToken);
            return new MutablePair<>(-4, null);
        }
        Account account;
        try {
            account = accountService.getAccount(openid, false);
        } catch (NotFollowingException e) {
            return new MutablePair<>(-3, null);
        }
        logger.info("accessToken:{},openId:{},account:{}", accessToken, openid, account);
        if (account == null) {
            return new MutablePair<>(-2, null);
        }


        Profile profile = accountService.getProfile(openid, false);
        Role role = permissionService.getRole(profile.getId());
        if (role == null) {
            // 获得用户的openid，根据openid查询用户的学号
            //如果报名了训练营或者开启了RISE,返回学生角色,反之返回陌生人
            List<ClassMember> classMembers = courseProgressService.loadActiveCourse(profile.getId());
            List<ImprovementPlan> plans = planService.loadUserPlans(openid);
            if (classMembers.isEmpty() && plans.isEmpty()) {
                role = Role.stranger();
            } else {
                role = Role.student();
            }
        }
        PCLoginUser pcLoginUser = new PCLoginUser();
        LoginUser loginUser = new LoginUser();
        loginUser.setId(profile.getId());
        loginUser.setOpenId(openid);
        loginUser.setHeadimgUrl(profile.getHeadimgurl());
        loginUser.setRealName(profile.getRealName());
        loginUser.setWeixinName(profile.getNickname());

        pcLoginUser.setWeixin(loginUser);
        pcLoginUser.setProfileId(profile.getId());
        pcLoginUser.setOpenId(loginUser.getOpenId());
        pcLoginUser.setRole(role.getId());
        pcLoginUser.setSignature(profile.getSignature());
        pcLoginUser.setPermissionList(permissionService.loadPermissions(role.getLevel()));
        logger.info("pcUser:{}", pcLoginUser);
        return new MutablePair<>(1, pcLoginUser);
    }

    public Role getUserRole(String openid){
        Role role = permissionService.getRole(openid);
        if (role == null) {
            // 获得用户的openid，根据openid查询用户的学号
            //如果报名了训练营或者开启了RISE,返回学生角色,反之返回陌生人
            List<ClassMember> classMembers = courseProgressService.loadActiveCourse(openid);
            List<ImprovementPlan> plans = planService.loadUserPlans(openid);
            if (classMembers.isEmpty() && plans.isEmpty()) {
                role = Role.stranger();
            } else {
                role = Role.student();
            }
        }
        return role;
    }
}
