package com.iquanwai.confucius.interceptor;

import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.util.CookieUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by justin on 16/8/26.
 */
public class CourseHandlerInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(!ConfigUtils.isDebug()) {
            String value = CookieUtils.getCookie(request, "_act");
            //没有access_token,跳转去授权
            if (StringUtils.isEmpty(value)) {
                String url = request.getRequestURL().toString();
                if(!StringUtils.isEmpty(request.getQueryString())){
                    url = url +"?"+request.getQueryString();
                }
                response.sendRedirect("/wx/oauth/auth?callbackUrl="+url);
                return false;
            }
        }
        return true;

    }

}
