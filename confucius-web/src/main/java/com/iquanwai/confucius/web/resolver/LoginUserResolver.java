package com.iquanwai.confucius.web.resolver;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.web.util.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by tomas on 3/17/16.
 */
public class LoginUserResolver implements HandlerMethodArgumentResolver {
    @Autowired
    private OAuthService oAuthService;
    @Autowired
    private AccountService accountService;

    private static Map<String, LoginUser> loginUserMap = Maps.newHashMap();

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return LoginUser.class.isAssignableFrom(methodParameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        //调试时，返回mock user
        if (ConfigUtils.isDebug()) {
            return LoginUser.defaultUser();
        }
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);

        if (request.getParameter("debug") != null && ConfigUtils.isFrontDebug()) {
            //前端调试开启时，如果debug=true,返回mockuser
            if ("true".equalsIgnoreCase(request.getParameter("debug"))) {
                return LoginUser.defaultUser();
            } else {
                //返回模拟的openid user
                return getLoginUser(request.getParameter("debug"));
            }
        }
        String state = CookieUtils.getCookie(request, OAuthService.WE_CHAT_STATE_COOKIE_NAME);
        if (loginUserMap.containsKey(state)) {
            return loginUserMap.get(state);
        }

        String openId = oAuthService.openId(state);
        if (StringUtils.isEmpty(openId)) {
            logger.error("accesstoken {} is not found in db", state);
            return null;
        }

        LoginUser loginUser = getLoginUser(openId);
        if (loginUser == null) {
            return null;
        }
        loginUserMap.put(state, loginUser);

        return loginUser;
    }

    private LoginUser getLoginUser(String openId) {
        Profile account = accountService.getProfile(openId, false);

        if (account == null || account.getNickname() == null) {
            logger.error("openId {} is not found in db", openId);
            return null;
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setId(account.getId());
        loginUser.setOpenId(account.getOpenid());
        loginUser.setWeixinName(account.getNickname());
        if (StringUtils.isNotEmpty(account.getHeadimgurl())) {
            loginUser.setHeadimgUrl(account.getHeadimgurl());
        } else {
            loginUser.setHeadimgUrl(Profile.DEFAULT_AVATAR);
        }
        loginUser.setRealName(account.getRealName());
        return loginUser;
    }

    public static LoginUser getLoginUser(HttpServletRequest request) {
        String accessToken = CookieUtils.getCookie(request, OAuthService.WE_CHAT_STATE_COOKIE_NAME);
        if (loginUserMap.containsKey(accessToken)) {
            return loginUserMap.get(accessToken);
        }
        return null;
    }
}
