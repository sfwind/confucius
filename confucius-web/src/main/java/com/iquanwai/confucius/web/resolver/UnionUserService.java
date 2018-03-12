package com.iquanwai.confucius.web.resolver;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.wx.CallbackDao;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.Role;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.web.util.CookieUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by 三十文
 */
@Service
public class UnionUserService {

    @Autowired
    private CallbackDao callbackDao;
    @Autowired
    private AccountService accountService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String PC_STATE_COOKIE_NAME = "_qt";
    private static final String MOBILE_STATE_COOKIE_NAME = "_act";
    private static final String MINI_STATE_HEADER_NAME = "sk";

    private static final int COOKIE_ALIVE_TIME = (int) TimeUnit.DAYS.toSeconds(7);

    private static final String PLATFORM_HEADER_NAME = "platform";

    /** 登录用户缓存 */
    private static Map<String, SoftReference<UnionUser>> unionUserCacheMap = Maps.newHashMap();
    /** 待更新信息用户的 unionId 集合 */
    private static List<String> waitRefreshUnionIds = Lists.newArrayList();

    /** 获取当前所在平台 */
    public UnionUser.Platform getPlatformType(HttpServletRequest request) {
        String platformHeader = request.getHeader(PLATFORM_HEADER_NAME);
        if (platformHeader == null) {
            // 资源请求，没有 platform header，查看 cookie 值
            String pcState = CookieUtils.getCookie(request, PC_STATE_COOKIE_NAME);
            if (pcState != null) {
                platformHeader = UnionUser.PlatformHeaderValue.PC_HEADER;
            }

            String mobileState = CookieUtils.getCookie(request, MOBILE_STATE_COOKIE_NAME);
            if (mobileState != null) {
                platformHeader = UnionUser.PlatformHeaderValue.MOBILE_HEADER;
            }
        }

        if (platformHeader != null) {
            switch (platformHeader) {
                case UnionUser.PlatformHeaderValue.PC_HEADER:
                    return UnionUser.Platform.PC;
                case UnionUser.PlatformHeaderValue.MOBILE_HEADER:
                    return UnionUser.Platform.MOBILE;
                case UnionUser.PlatformHeaderValue.MINI_HEADER:
                    return UnionUser.Platform.MINI;
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    /** 根据请求获取 callback 数据 */
    public Callback getCallbackByRequest(HttpServletRequest request) {
        UnionUser.Platform platform = getPlatformType(request);
        if (platform == null) {
            return null;
        }
        switch (platform) {
            case PC:
                String pcState = CookieUtils.getCookie(request, PC_STATE_COOKIE_NAME);
                return callbackDao.queryByState(pcState);
            case MOBILE:
                String mobileState = CookieUtils.getCookie(request, MOBILE_STATE_COOKIE_NAME);
                return callbackDao.queryByState(mobileState);
            case MINI:
                String miniState = request.getHeader(MINI_STATE_HEADER_NAME);
                return callbackDao.queryByState(miniState);
            default:
                return null;
        }
    }

    public UnionUser getUnionUserByCallback(Callback callback) {
        UnionUser unionUser;
        String state = callback.getState();
        String unionId = callback.getUnionId();
        if (unionUserCacheMap.containsKey(state)) {
            unionUser = unionUserCacheMap.get(state).get();
            if (unionUser == null) {
                // 如果软连接对象过期，重新加载用户信息
                unionUser = getUnionUserByUnionId(unionId);
                unionUserCacheMap.put(state, new SoftReference<>(unionUser));
            } else {
                if (waitRefreshUnionIds.contains(unionId)) {
                    unionUser = getUnionUserByUnionId(unionId);
                    unionUserCacheMap.put(state, new SoftReference<>(unionUser));
                    waitRefreshUnionIds.remove(unionId);
                }
            }
        } else {
            unionUser = getUnionUserByUnionId(unionId);
            unionUserCacheMap.put(state, new SoftReference<>(unionUser));
        }
        return unionUser;
    }

    public boolean isDocumentRequest(HttpServletRequest request) {
        return request.getHeader(PLATFORM_HEADER_NAME) == null;
    }

    public void addCookie(UnionUser.Platform platform, String state, HttpServletResponse response) {
        switch (platform) {
            case PC:
                CookieUtils.addCookie(PC_STATE_COOKIE_NAME, state, COOKIE_ALIVE_TIME, response);
                break;
            case MOBILE:
                CookieUtils.addCookie(MOBILE_STATE_COOKIE_NAME, state, COOKIE_ALIVE_TIME, response);
                break;
            case MINI:
                break;
            default:
                break;
        }
    }

    public void removeCookie(UnionUser.Platform platform, HttpServletResponse response) {
        switch (platform) {
            case PC:
                CookieUtils.removeCookie(PC_STATE_COOKIE_NAME, ConfigUtils.realDomainName(), response);
                break;
            case MOBILE:
                CookieUtils.removeCookie(MOBILE_STATE_COOKIE_NAME, ConfigUtils.realDomainName(), response);
                break;
            case MINI:
                break;
            default:
                break;
        }
    }

    /**
     * 删除当前用户的缓存信息
     */
    public void logout(String state) {
        unionUserCacheMap.remove(state);
    }

    /**
     * 根据 unionId 获取用户对象，如果 Profile 不存在，会从微信获取
     * @param unionId 联合 UnionId
     */
    private UnionUser getUnionUserByUnionId(String unionId) {
        UnionUser unionUser = new UnionUser();
        Profile profile = accountService.getProfileByUnionId(unionId);
        Assert.notNull(profile, "Profile 不能为空");
        unionUser.setId(profile.getId());
        unionUser.setOpenId(profile.getOpenid());
        unionUser.setUnionId(profile.getUnionid());
        unionUser.setNickName(profile.getNickname());
        unionUser.setHeadImgUrl(profile.getHeadimgurl());

        Role role = accountService.getUserRole(profile.getId());
        if (role != null) {
            unionUser.setRoleId(role.getId());
        }
        return unionUser;
    }

}
