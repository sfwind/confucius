package com.iquanwai.confucius.web.interceptor;

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

    /**
     * 只校验 callback 是否存在，对于 callback 是否过期的情况，需要在 resolver 里面自行重新刷新、或者让用户重新授权
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.info("进入拦截器");

        UnionUser.Platform platform = unionUserService.getPlatformType(request);
        if (platform == null || unionUserService.isDocumentRequest(request)) {
            logger.info("platform 为空或者当前请求的是资源请求");
            return true;
        } else {
            Callback callback = unionUserService.getCallbackByRequest(request);
            if (callback != null && callback.getUnionId() != null) {
                return true;
            } else {
                if (ConfigUtils.isDebug()) {
                    return true;
                } else {
                    return handleUnLogin(response);
                }
            }
        }
    }

    /**
     * 对于 ajax 请求，不存在 callback 请求的处理
     * @param response 响应
     * @return 是否通过拦截器
     */
    private boolean handleUnLogin(HttpServletResponse response) throws Exception {
        logger.info("不存在 callback，特殊处理请求");
        writeUnLoginStatus(response);
        return false;
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

}
