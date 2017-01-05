package com.iquanwai.confucius.web.mobile.controller;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.exception.ErrorConstants;
import com.iquanwai.confucius.biz.util.*;
import com.iquanwai.confucius.resolver.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created by nethunder on 2016/12/19.
 */
@RestController
@RequestMapping("/mobile")
public class MobileLoginController {
    private static String CHECK_RESULT_URL = "/account/login/result/";
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private RestfulHelper restfulHelper;

    @PostConstruct
    public void initConfig(){
        CHECK_RESULT_URL = ConfigUtils.domainName()+CHECK_RESULT_URL;
    }

    /**
     * 记得加入拦截器排除
     *
     */
    @RequestMapping("/login/check")
    public void loginCheck(@RequestParam(name = "sign", required = true) String sign,
                           @RequestParam(name = "t", required = true) String time,
                           @RequestParam(name = "s", required = true) String sessionId,
                           HttpServletRequest request,
                           LoginUser loginUser,
                           HttpServletResponse response)
    {
        try {
            if(loginUser==null){
                logger.error("扫码登录失败，用户信息不能为空");
                response.sendRedirect(ConfigUtils.adapterDomainName()+"/static/login/error?err=获取信息失败，请重试");
                return;
            }
            // 校验是否过期
            long interval = DateUtils.currentTimestamp() - Long.parseLong(time);
            if (interval > 60) {
                // 该链接超过一分钟，已失效
                //HTTP通知PC
                Map<String, Object> map = Maps.newHashMap();
                map.put("sessionId", sessionId);
                map.put("status", Constants.Status.FAIL);
                map.put("error", ErrorConstants.SESSION_TIME_OUT);
                String body = restfulHelper.post(CHECK_RESULT_URL, CommonUtils.mapToJson(map));
                if ("".equals(body)) {
                    // 刷新失败
                    logger.error("刷新验证码失败");
                    response.sendRedirect(ConfigUtils.adapterDomainName()+"/static/login/error?err=刷新验证码失败,请手动刷新PC页面");
                    return;
                } else {
                    response.sendRedirect(ConfigUtils.adapterDomainName()+"/static/login/error?err=二维码超时，已自动刷新，请重新扫描");
                    return;

                }
            }
            // 未超时，开始校验
            Map<String, String> map = Maps.newHashMap();
            map.put("sessionid", sessionId);
            map.put("salt", ConfigUtils.getLoginSalt());
            map.put("unix_timestamp", time);
            map.put("uri", request.getRequestURI());
            String checkSign = CommonUtils.jsSign(map);
            if (checkSign.equals(sign)) {
                // 校验通过
                Map<String, Object> map1 = Maps.newHashMap();
                map1.put("sessionId", sessionId);
                map1.put("status", Constants.Status.OK);
                map1.put("loginUser", loginUser);
                String body1 = restfulHelper.post(CHECK_RESULT_URL, CommonUtils.mapToJson(map1));
                if (!"".equals(body1)) {
                    // PC端处理结果成功
                    Map<String, Object> result = CommonUtils.jsonToMap(body1);
                    // 解析处理结果
                    logger.info("PC登录校验成功.{}",result);
                    response.sendRedirect(ConfigUtils.adapterDomainName()+"/static/login/result");
                    return;
                } else {
                    // PC端登录失败
                    logger.error("PC端登录失败,sessionId:" + sessionId);
                    response.sendRedirect(ConfigUtils.adapterDomainName()+"/static/login/result?err=登录异常，请联系管理员");
                    return;
                }
            } else {
                // 移动端校验失败，链接无效
                logger.error("移动端校验失败，链接无效");
                response.sendRedirect(ConfigUtils.adapterDomainName()+"/static/login/result?err=登录失败，信息校验错误");
                return;
            }
        } catch (Exception e){
            logger.error("处理登录结果失败",e);
            if(!e.getClass().equals(IOException.class)){
                try {
                    response.sendRedirect(ConfigUtils.adapterDomainName()+"/static/login/result?err="+e.getLocalizedMessage());
                } catch (IOException e1) {
                    logger.error("校验二维码，移动端重定向失败");
                }
            }
        }
    }
}
