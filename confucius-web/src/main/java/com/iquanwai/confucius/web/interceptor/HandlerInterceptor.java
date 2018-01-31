package com.iquanwai.confucius.web.interceptor;

import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.web.resolver.UnionUser;
import com.iquanwai.confucius.web.resolver.UnionUserService;
import com.iquanwai.confucius.web.util.WebUtils;
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
        if (platform == null) {
            // platform 为空，请求的是资源或者页面，在 IndexController 有相关页面匹配，此处仅做补充处理
            logger.info("platform 为空");
            return true;
        } else {
            Callback callback = unionUserService.getCallbackByRequest(request);
            boolean approveTag = (callback != null && callback.getUnionId() != null) || handleUnLogin(platform, request, response);
            if (approveTag && platform == UnionUser.Platform.PC) {
                UnionUser unionUser = unionUserService.getUnionUserByCallback(callback);
                if (unionUser.getOpenId() == null) {
                    response.sendRedirect("/servercode");
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }
    }

    /**
     * 对于 ajax 请求，不存在 callback 请求的处理
     * @param platform 平台信息
     * @param request 请求
     * @param response 响应
     * @return 是否通过拦截器
     */
    private boolean handleUnLogin(UnionUser.Platform platform, HttpServletRequest request, HttpServletResponse response) throws Exception {
        switch (platform) {
            case PC:
                writeUnLoginStatus(response);
                break;
            case MOBILE:
                // 静默授权
                WebUtils.auth(request, response);
                break;
            case MINI:
                writeUnLoginStatus(response);
                break;
            default:
                break;
        }
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
