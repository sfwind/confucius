package com.iquanwai.confucius.web.resolver;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.wx.CallbackDao;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;

/**
 * Created by 三十文
 */
public class WeMiniLoginUserResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private CallbackDao callbackDao;
    @Autowired
    private AccountService accountService;

    private static Map<String, WeMiniLoginUser> weMiniLoginUserMap = Maps.newHashMap();

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return WeMiniLoginUser.class.isAssignableFrom(methodParameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        logger.info("进入 WeMiniLoginUser Resolver");
        // if (ConfigUtils.isDebug()) {
        //     return WeMiniLoginUser.defaultUser();
        // }

        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            logger.info("key : {} value: {}", key, request.getHeader(key));
        }

        String state = request.getHeader("sk");
        logger.info("接收 state：{}", state);
        if (weMiniLoginUserMap.containsKey(state)) {
            logger.info("读取缓存：{}", state);
            return weMiniLoginUserMap.get(state);
        } else {
            logger.info("开始读取 callback");
            Callback callback = callbackDao.queryByState(state);
            if (callback == null) {
                logger.info("call back 为空");
                return null;
            } else {
                WeMiniLoginUser weMiniLoginUser = new WeMiniLoginUser();

                logger.info("开始打印 weMiniLoginUser：");
                ReflectionToStringBuilder.toString(weMiniLoginUser, ToStringStyle.MULTI_LINE_STYLE);

                String unionId = callback.getUnionId();
                weMiniLoginUser.setUnionId(callback.getUnionId());
                weMiniLoginUser.setWeMiniOpenId(callback.getWeMiniOpenid());

                Profile profile = accountService.queryByUnionId(unionId);
                logger.info("根据 unionId 获取 profile");
                if (profile != null) {
                    logger.info("profile 不存在");
                    weMiniLoginUser.setId(profile.getId());
                    weMiniLoginUser.setOpenId(profile.getOpenid());
                    // 放入缓存
                    weMiniLoginUserMap.put(state, weMiniLoginUser);
                }
                logger.info("最后输出 weMiniLoginUser");
                ReflectionToStringBuilder.toString(weMiniLoginUser, ToStringStyle.MULTI_LINE_STYLE);
                return weMiniLoginUser;
            }
        }
    }

}
