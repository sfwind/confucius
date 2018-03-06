package com.iquanwai.confucius.web.resolver;

import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
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

public class PCLoginUserResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private UnionUserService unionUserService;
    @Autowired
    private AccountService accountService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return PCLoginUser.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        if (ConfigUtils.isDebug()) {
            return PCLoginUser.defaultUser();
        }

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        Callback callback = unionUserService.getCallbackByRequest(request);
        if (callback == null) return null;

        UnionUser unionUser = unionUserService.getUnionUserByCallback(callback);
        return adapterUnionUser(unionUser);
    }

    private PCLoginUser adapterUnionUser(UnionUser unionUser) {
        PCLoginUser pcLoginUser = new PCLoginUser();
        pcLoginUser.setId(unionUser.getId());
        pcLoginUser.setProfileId(unionUser.getId());
        pcLoginUser.setOpenId(unionUser.getOpenId());
        pcLoginUser.setSignature("");
        pcLoginUser.setRole(accountService.getUserRole(unionUser.getId()).getId());

        LoginUser loginUser = new LoginUser();
        loginUser.setId(unionUser.getId());
        loginUser.setOpenId(unionUser.getOpenId());
        loginUser.setWeixinName(unionUser.getNickName());
        loginUser.setHeadimgUrl(unionUser.getHeadImgUrl());
        loginUser.setRealName("");

        pcLoginUser.setWeixin(loginUser);
        return pcLoginUser;
    }

}
