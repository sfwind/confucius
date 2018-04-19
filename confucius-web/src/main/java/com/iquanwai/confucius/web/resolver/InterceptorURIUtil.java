package com.iquanwai.confucius.web.resolver;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by 三十文
 */
public class InterceptorURIUtil {

    /**
     * 获取所有需要用户身份拦截的资源请求 uri
     * @return
     */
    public static List<String> getInterceptorUriRegexs() {
        List<String> regexs = Lists.newArrayList();
        regexs.add("/survey/wjx");
        regexs.add("/redirect/template/message");
        return regexs;
    }

}
