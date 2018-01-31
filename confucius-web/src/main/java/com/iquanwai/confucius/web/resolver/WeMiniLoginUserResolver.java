package com.iquanwai.confucius.web.resolver;

import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by 三十文
 */
public class WeMiniLoginUserResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private UnionUserService unionUserService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return WeMiniLoginUser.class.isAssignableFrom(methodParameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {

        if (ConfigUtils.isDebug()) {
            return WeMiniLoginUser.defaultUser();
        }

        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);

        Callback callback = unionUserService.getCallbackByRequest(request);
        if (callback == null) return null;

        UnionUser unionUser = unionUserService.getUnionUserByCallback(callback);
        logger.info("获取 adapter weMiniLoginUser 用户，id：{}", unionUser.getId());
        return adapterUnionUser(unionUser);
    }

    private WeMiniLoginUser adapterUnionUser(UnionUser unionUser) {
        WeMiniLoginUser weMiniLoginUser = new WeMiniLoginUser();
        weMiniLoginUser.setId(unionUser.getId());
        weMiniLoginUser.setOpenId(unionUser.getOpenId());
        weMiniLoginUser.setNickName(unionUser.getNickName());
        weMiniLoginUser.setHeadImgUrl(unionUser.getHeadImgUrl());
        return weMiniLoginUser;
    }

}
