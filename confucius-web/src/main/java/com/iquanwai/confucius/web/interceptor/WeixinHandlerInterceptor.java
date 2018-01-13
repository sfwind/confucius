package com.iquanwai.confucius.web.interceptor;

import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.web.util.CookieUtils;
import com.iquanwai.confucius.web.util.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by justin on 16/8/26.
 */
public class WeixinHandlerInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!ConfigUtils.isDebug()) {
            // 前端debug开启时,不校验
            if (request.getParameter("debug") != null && ConfigUtils.isFrontDebug()) {
                return true;
            }
            String value = CookieUtils.getCookie(request, OAuthService.ACCESS_TOKEN_COOKIE_NAME);
            //没有access_token,跳转去授权
            if (StringUtils.isEmpty(value)) {
                WebUtils.auth(request, response);
                return false;
            }
        }
        return true;
    }

}
