package com.iquanwai.confucius.web.resolver;

import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by 三十文
 */
public class UnionUserResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private UnionUserService unionUserService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return UnionUser.class.isAssignableFrom(methodParameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        logger.info("进入 UnionUser Resolver");

        if (ConfigUtils.isDebug()) {
            return UnionUser.defaultUser();
        }

        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        Callback callback = unionUserService.getCallbackByRequest(request);
        // callback 为空的话，会在 interceptor 那层拦截掉
        Assert.notNull(callback, "callback 不能为空");
        Assert.notNull(callback.getUnionId(), "callback 的 UnionId 不能为空");

        UnionUser unionUser = unionUserService.getUnionUserByCallback(callback);
        logger.info("加载 UnionUserId: {}, UnionId: {}", unionUser.getId(), unionUser.getUnionId());
        return unionUser;
    }

}
