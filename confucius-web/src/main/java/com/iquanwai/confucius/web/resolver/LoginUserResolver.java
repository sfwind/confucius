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

// TODO 利用 UnionUser 作为中间转换
public class LoginUserResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private UnionUserService unionUserService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return LoginUser.class.isAssignableFrom(methodParameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        if (ConfigUtils.isDebug()) {
            logger.info("处于 debug 模式");
            return LoginUser.defaultUser();
        }

        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        Callback callback = unionUserService.getCallbackByRequest(request);
        // callback 为空的话，会在 interceptor 那层拦截掉
        Assert.notNull(callback, "callback 不能为空");
        Assert.notNull(callback.getUnionId(), "callback 的 UnionId 不能为空");

        UnionUser unionUser = unionUserService.getUnionUserByCallback(callback);
        LoginUser loginUser = adapterUnionUser(unionUser);
        logger.info("获取 adapter loginUser 用户，id：{}", loginUser.getId());
        return adapterUnionUser(unionUser);
    }

    public LoginUser adapterUnionUser(UnionUser unionUser) {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(unionUser.getId());
        loginUser.setOpenId(unionUser.getOpenId());
        loginUser.setWeixinName(unionUser.getNickName());
        loginUser.setHeadimgUrl(unionUser.getHeadImgUrl());
        loginUser.setRealName("");
        return loginUser;
    }

}
