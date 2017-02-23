package com.iquanwai.confucius.web.interceptor;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.permission.PermissionService;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.web.resolver.PCLoginUserResolver;
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
    private PermissionService permissionService;

    public PCAjaxHandlerInterceptor() {

    }

    public PCAjaxHandlerInterceptor(PermissionService permissionService) {
        this.permissionService = permissionService;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!ConfigUtils.isDebug()) {
            // 获取sessionId
            String value = request.getRequestedSessionId();
            // 没有session信息
            if (StringUtils.isEmpty(value) || !PCLoginUserResolver.isLogin(value)) {
                Map<String, Object> map = Maps.newHashMap();
                PrintWriter out = response.getWriter();
                map.put("code", 401);
                map.put("msg", "没有登录");
                out.write(CommonUtils.mapToJson(map));
                return false;
            }

            // 查看权限
//            PCLoginUser pcLoginUser = PCLoginUserResolver.getLoginUser(value);
//            String role = pcLoginUser.getRole();
//            // 根据role查询所有权限列表
//            if (!permissionService.checkPermission(role, request.getRequestURI())) {
//                PrintWriter out = response.getWriter();
//                Map<String, Object> map = Maps.newHashMap();
//                map.put("code", 403);
//                map.put("msg", "没有该权限");
//                out.write(CommonUtils.mapToJson(map));
//                return false;
//            }
        }
        return true;
    }

}
