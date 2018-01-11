package com.iquanwai.confucius.web.resolver;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.wx.CallbackDao;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
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
        if (ConfigUtils.isDebug()) {
            return WeMiniLoginUser.defaultUser();
        }

        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        String state = request.getHeader("_sk");
        if (weMiniLoginUserMap.containsKey(state)) {
            return weMiniLoginUserMap.get(state);
        } else {
            Callback callback = callbackDao.queryByState(state);
            if (callback == null) {
                return null;
            } else {
                WeMiniLoginUser weMiniLoginUser = new WeMiniLoginUser();

                String unionId = callback.getUnionId();
                weMiniLoginUser.setUnionId(callback.getUnionId());
                weMiniLoginUser.setWeMiniOpenId(callback.getWeMiniOpenid());

                Profile profile = accountService.queryByUnionId(unionId);
                if (profile != null) {
                    weMiniLoginUser.setId(profile.getId());
                    weMiniLoginUser.setOpenId(profile.getOpenid());
                    // 放入缓存
                    weMiniLoginUserMap.put(state, weMiniLoginUser);
                }
                return weMiniLoginUser;
            }
        }
    }

}
