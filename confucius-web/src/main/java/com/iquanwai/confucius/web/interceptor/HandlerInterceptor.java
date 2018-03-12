package com.iquanwai.confucius.web.interceptor;

import com.iquanwai.confucius.biz.domain.permission.PermissionService;
import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.web.resolver.UnionUser;
import com.iquanwai.confucius.web.resolver.UnionUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by 三十文
 */
public class HandlerInterceptor extends HandlerInterceptorAdapter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UnionUserService unionUserService;
    @Autowired
    private PermissionService permissionService;

    /**
     * 只校验 callback 是否存在，对于 callback 是否过期的情况，需要在 resolver 里面自行重新刷新、或者让用户重新授权
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UnionUser.Platform platform = unionUserService.getPlatformType(request);
        if (platform == null || unionUserService.isDocumentRequest(request)) {
            return true;
        } else {
            // if (ConfigUtils.isDebug()) {
            //     String requestUrl = request.getRequestURI();
            //     Callback callback = new Callback();
            //     callback.setState("qrm4zqjlqlrjlr9oo8qfuqy6t1kan6k8");
            //     callback.setUnionId("os8zavwgXriKmPo0Crjcl0Kn8Nq8");
            //     UnionUser unionUser = unionUserService.getUnionUserByCallback(callback);
            //     boolean permission = permissionService.checkPermission(unionUser.getRoleId(), requestUrl);
            //     if (!permission) {
            //         writeNoAuthority(response);
            //         return false;
            //     }
            // }

            Callback callback = unionUserService.getCallbackByRequest(request);
            if (callback != null && callback.getUnionId() != null) {
                // 校验是否有权限访问页面
                String requestUrl = request.getRequestURI();
                logger.info(requestUrl);
                UnionUser unionUser = unionUserService.getUnionUserByCallback(callback);
                if (unionUser != null) {
                    boolean authority = permissionService.checkPermission(unionUser.getRoleId(), requestUrl);
                    if (!authority) {
                        writeNoAuthority(response);
                        return false;
                    }
                }
            } else {
                if (ConfigUtils.isDebug()) {
                    return true;
                }
                writeUnLoginStatus(response);
                return false;
            }
            return false;
        }
    }

    /**
     * ajax 请求 登录校验不通过，返回未登录 700 状态码
     */
    private void writeUnLoginStatus(HttpServletResponse response) throws IOException {
        Writer writer = null;
        try {
            response.setStatus(700);
            writer = response.getWriter();
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * ajax 请求接口没有权限，返回 701 状态码
     */
    private void writeNoAuthority(HttpServletResponse response) throws IOException {
        Writer writer = null;
        try {
            response.setStatus(701);
            writer = response.getWriter();
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

}
