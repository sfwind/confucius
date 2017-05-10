package com.iquanwai.confucius.web.interceptor;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.util.CommonUtils;
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
import java.io.PrintWriter;
import java.util.Map;

/**
 * Created by nethunder on 2017/1/7.
 */
public class PCAjaxHandlerInterceptor extends HandlerInterceptorAdapter {
    private LoginUserService loginUserService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public PCAjaxHandlerInterceptor() {

    }

    public PCAjaxHandlerInterceptor(LoginUserService loginUserService) {
        this.loginUserService = loginUserService;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!ConfigUtils.isDebug()) {

            // 获取sessionId
            String value = CookieUtils.getCookie(request, OAuthService.QUANWAI_TOKEN_COOKIE_NAME);
            boolean cookieInvalid = false;
            // 没有session信息
            if (StringUtils.isEmpty(value)) {
                cookieInvalid = true;
            } else {
                // cookie 不为空
                if (!loginUserService.isLogin(value)) {
                    // 有cookie，但是没有登录
                    Pair<Integer, Callback> pair = loginUserService.refreshLogin(value);
                    if (pair.getLeft() < 1) {
                        cookieInvalid = true;
                    }
                    // 否则通过
                }
            }
            if(cookieInvalid){
                Map<String, Object> map = Maps.newHashMap();
                PrintWriter out = response.getWriter();
                map.put("code", 401);
                map.put("msg", "没有登录");
                out.write(CommonUtils.mapToJson(map));
                return false;
            }

            // 查看权限
            Pair<Integer,PCLoginUser> pair = loginUserService.getLoginUser(value);
            if (pair.getLeft() < 1) {
                logger.error("登录信息异常：_qt:{},result:{}", value, pair);
                WebUtils.login(request, response);
                return false;
            }

            PCLoginUser pcLoginUser = pair.getRight();
            Integer role = pcLoginUser.getRole();
            // 根据role查询所有权限列表
            if (!loginUserService.checkPermission(role, request.getRequestURI())) {
                logger.error("权限检查失败,用户:{},role:{},url:{}", pcLoginUser.getOpenId(), role, request.getRequestURI());
                PrintWriter out = response.getWriter();
                Map<String, Object> map = Maps.newHashMap();
                map.put("code", 403);
                map.put("msg", "没有该权限");
                out.write(CommonUtils.mapToJson(map));
                return false;
            }
        }

        // 全部检查结束，返回true
        return true;
    }

}
