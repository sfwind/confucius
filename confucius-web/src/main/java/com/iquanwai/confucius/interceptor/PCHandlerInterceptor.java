package com.iquanwai.confucius.interceptor;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.permission.PermissionService;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.resolver.PCLoginUser;
import com.iquanwai.confucius.resolver.PCLoginUserResolver;
import com.iquanwai.confucius.util.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Created by nethunder on 2016/12/23.
 */
public class PCHandlerInterceptor extends HandlerInterceptorAdapter {

    private PermissionService permissionService;

    public PCHandlerInterceptor() {

    }

    public PCHandlerInterceptor(PermissionService permissionService) {
        this.permissionService = permissionService;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!ConfigUtils.isDebug()) {
            // 获取sessionId
            String value = request.getRequestedSessionId();
            // 没有session信息
            if (StringUtils.isEmpty(value) || !PCLoginUserResolver.isLogin(value)) {
                WebUtils.login(request, response);
                return false;
            }

            // 查看权限
            PCLoginUser pcLoginUser = PCLoginUserResolver.getLoginUser(value);
            String role = pcLoginUser.getRole();
            // 根据role查询所有权限列表
            if (!permissionService.checkPermission(role, request.getRequestURI())) {
                WebUtils.reject(request,response);
                return false;
            }
        }
        return true;
    }

}
