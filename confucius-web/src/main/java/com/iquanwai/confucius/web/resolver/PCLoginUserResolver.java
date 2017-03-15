package com.iquanwai.confucius.web.resolver;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.web.account.websocket.LoginEndpoint;
import com.iquanwai.confucius.web.util.CookieUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.lang.ref.SoftReference;
import java.util.Map;

/**
 * Created by nethunder on 2016/12/23.
 */
public class PCLoginUserResolver implements HandlerMethodArgumentResolver {
    /**
     * 缓存已经登录的用户
     */
    private static Map<String, SoftReference<PCLoginUser>> pcLoginUserMap = Maps.newHashMap();

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (PCLoginUser.class.isAssignableFrom(parameter.getParameterType())) {
            return true;
        }
        return false;
    }

    public static int count(){
        return pcLoginUserMap.size();
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        //调试时，返回mock user
        if (ConfigUtils.isDebug()) {
            return PCLoginUser.defaultUser();
        }
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        //前端调试开启时，返回mock user
        if (request.getParameter("debug") != null && ConfigUtils.isFrontDebug()) {
            return PCLoginUser.defaultUser();
        }
        String pcToken = CookieUtils.getCookie(request, LoginEndpoint.QUANWAI_TOKEN_COOKIE_NAME);
        if (pcLoginUserMap.containsKey(pcToken)) {
            return pcLoginUserMap.get(pcToken).get();
        }
        // 只能从缓存中获得用户信息，只有一个登录入口
        return null;
    }

    /**
     * 登录，就是缓存起来
     * @param sessionId sessionId,这个sessionIds是三个点拼起来的
     * @param pcLoginUser 用户
     */
    public static void login(String sessionId, PCLoginUser pcLoginUser) {
        SoftReference<PCLoginUser> temp = new SoftReference<PCLoginUser>(pcLoginUser);
        pcLoginUserMap.put(sessionId, temp);
    }

    /**
     * 根据sessionId判断用户是否登录
     * @param sessionId SessionId
     * @return  是否登录
     */
    public static boolean isLogin(String sessionId){
        SoftReference<PCLoginUser> softReference = pcLoginUserMap.get(sessionId);
        if(softReference!=null){
            PCLoginUser pcLoginUser = softReference.get();
            if(pcLoginUser!=null){
                return true;
            } else {
                // 清理
                pcLoginUserMap.remove(sessionId);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 获得登录的用户信息
     * @param sessionId sessionId
     * @return 登录的用户
     */
    public static PCLoginUser getLoginUser(String sessionId){
        return pcLoginUserMap.get(sessionId).get();
    }

    public static PCLoginUser getLoginUser(HttpServletRequest request){
        String sessionId = request.getRequestedSessionId();
        if(pcLoginUserMap.containsKey(sessionId)){
            return pcLoginUserMap.get(sessionId).get();
        }
        return null;
    }
}
