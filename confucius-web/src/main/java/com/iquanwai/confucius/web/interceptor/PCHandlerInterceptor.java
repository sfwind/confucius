package com.iquanwai.confucius.web.interceptor;

import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.web.pc.LoginUserService;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.CookieUtils;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by nethunder on 2016/12/23.
 */
public class PCHandlerInterceptor extends HandlerInterceptorAdapter {

    private LoginUserService loginUserService;

    public PCHandlerInterceptor() {

    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    public PCHandlerInterceptor(LoginUserService loginUserService) {
        this.loginUserService = loginUserService;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!ConfigUtils.isDebug()) {
            // 获取sessionId
            String value = CookieUtils.getCookie(request, OAuthService.PC_STATE_COOKIE_NAME);
            if (StringUtils.isEmpty(value)) {
                // 没有session信息,跳转到登录页面
                logger.error("no cookie,go to login page");
                WebUtils.login(request, response);
                return false;
            }
            if (!loginUserService.isLogin(value)) {
                // 有cookie，但是没有登录

                Pair<Integer, Callback> pair = loginUserService.refreshLogin(value);
                if (pair.getLeft() == -1) {
                    logger.error("clean _qt cookie:{}", value);
                    CookieUtils.removeCookie(OAuthService.PC_STATE_COOKIE_NAME, response);
                    WebUtils.login(request, response);
                    return false;
                } else if(pair.getLeft() == -2){
                    response.sendRedirect(ConfigUtils.adapterDomainName() + "/pc/static/error?err=请先关注服务号");
                    CookieUtils.removeCookie(OAuthService.PC_STATE_COOKIE_NAME, response);
                    WebUtils.login(request, response);
                    return false;
                }
                // 否则通过
            }

            // 查看权限
            Pair<Integer,PCLoginUser> pair = loginUserService.getLoginUser(value);
            if (pair.getLeft() < 1) {
                logger.error("登录信息异常：_qt:{},result:{}", value, pair);
                WebUtils.login(request, response);
                return false;
            } else {
                PCLoginUser pcLoginUser = pair.getRight();
                Integer role = pcLoginUser.getRole();
                // 根据role查询所有权限列表
                if (!loginUserService.checkPermission(role, request.getRequestURI())) {
                    logger.error("document handler 权限检查失败,用户:{},role:{},url:{}", pcLoginUser.getOpenId(), role, request.getRequestURI());
                    WebUtils.reject(request,response);
                    return false;
                }
            }
        }
        return true;
    }

}
