package com.iquanwai.confucius.web.resolver;

import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.web.account.websocket.LoginEndpoint;
import com.iquanwai.confucius.web.pc.LoginUserService;
import com.iquanwai.confucius.web.util.CookieUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by nethunder on 2016/12/23.
 */
public class PCLoginUserResolver implements HandlerMethodArgumentResolver {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private LoginUserService loginUserService;

    public PCLoginUserResolver(){
    }

    public PCLoginUserResolver(LoginUserService loginUserService) {
        this.loginUserService = loginUserService;
    }


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (PCLoginUser.class.isAssignableFrom(parameter.getParameterType())) {
            return true;
        }
        return false;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        //调试时，返回mock user
        if (ConfigUtils.isDebug()) {
            return PCLoginUser.defaultUser();
        }
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        //前端调试开启时，返回mock user
        if (request.getParameter("debug") != null && ConfigUtils.isFrontDebug()) {
            return PCLoginUser.defaultUser();
        }
        Pair<Integer, PCLoginUser> loginUser = loginUserService.getLoginUser(request);
        if (loginUser.getLeft() < 1) {
            String remoteIp = request.getHeader("X-Forwarded-For");
            String value = CookieUtils.getCookie(request, LoginEndpoint.QUANWAI_TOKEN_COOKIE_NAME);
            logger.error("没有找到用户,ip:{},_qt:{}", remoteIp, value);
        }
        return loginUser.getRight();
    }

}
