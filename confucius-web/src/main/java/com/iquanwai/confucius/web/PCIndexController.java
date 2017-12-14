package com.iquanwai.confucius.web;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQPublisher;
import com.iquanwai.confucius.mq.CustomerReceiver;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.CookieUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Map;

/**
 * Created by nethunder on 2017/1/3.
 */
@Controller
public class PCIndexController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    private RabbitMQPublisher mqPublisher;

    @PostConstruct
    public void init(){
        mqPublisher = rabbitMQFactory.initFanoutPublisher(CustomerReceiver.TOPIC);
    }

    @RequestMapping(value = "/pc/static/**")
    public ModelAndView getStatic(HttpServletRequest request, PCLoginUser pcLoginUser) {
        return pcView(request, pcLoginUser);
    }

    /**
     * 前往碎片化页面
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/fragment/**")
    public ModelAndView getFragmentPage(HttpServletRequest request, PCLoginUser pcLoginUser) {
        return pcView(request, pcLoginUser);
    }

    /**
     * 前往后台页面
     *
     * @param request
     * @return
     */
    @RequestMapping(value = {"/backend/**", "/asst/**"})
    public ModelAndView getBackendPage(HttpServletRequest request, PCLoginUser pcLoginUser) {
        return pcView(request, pcLoginUser, "backend");
    }

    /**
     * 前往home页面
     */
    @RequestMapping(value = {"/home", "/"}, method = RequestMethod.GET)
    public ModelAndView getHome(HttpServletRequest request, PCLoginUser pcLoginUser) {
        return ConfigUtils.isDevelopment() ? new ModelAndView("index") : new ModelAndView("home");
    }

    @RequestMapping(value = "/servercode")
    public ModelAndView getServerCodePage(HttpServletRequest request, PCLoginUser pcLoginUser) {
        return pcView(request, pcLoginUser);
    }

    /**
     * 前往登录页面
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView getLoginPage(HttpServletRequest request, PCLoginUser pcLoginUser) {
        return pcView(request, pcLoginUser);
    }

    /**
     * 用户请求注销
     * 1、删除本地 cookie 数据
     * 2、MQ 发送广播，删除其余节点上的 cookie 数据
     */
    @RequestMapping(value = "/logout")
    public void getLogoutPage(HttpServletRequest request, HttpServletResponse response, PCLoginUser pcLoginUser) throws IOException {
        // 获取当前链接 cookie 的值，作为删除 cookieMap 的 key 值
        String cookie = CookieUtils.getCookie(request, OAuthService.QUANWAI_TOKEN_COOKIE_NAME);
        System.out.println("cookie = " + cookie);
        logger.info("cookie:" + cookie);
        if(cookie == null) {
            response.sendRedirect("/login");
            return;
        }
        logger.info(ConfigUtils.domainName());
        // 1、删除 cookie
        CookieUtils.removeCookie(OAuthService.QUANWAI_TOKEN_COOKIE_NAME, ConfigUtils.realDomainName(), response);
        try {
            // 2、通过 MQ 发送广播，删除每个节点上的 cookie 数据
            mqPublisher.publish(cookie);
        } catch(ConnectException e) {
            logger.error(e.getLocalizedMessage());
        }
        response.sendRedirect("/login");
    }

    private ModelAndView pcView(HttpServletRequest request, PCLoginUser pcLoginUser) {
        return pcView(request, pcLoginUser, "site");
    }

    private ModelAndView pcView(HttpServletRequest request, PCLoginUser pcLoginUser, String view) {
        ModelAndView mav = new ModelAndView(view);
        if(request.getParameter("debug") != null) {
            if(ConfigUtils.isFrontDebug()) {
                mav.addObject("resource", "http://0.0.0.0:4000/pc_bundle.js");
                mav.addObject("loginSocketUrl", "127.0.0.1:8080/session");
            } else {
                mav.addObject("resource", ConfigUtils.staticPcResourceUrl(null));
            }
        } else {
            mav.addObject("resource", ConfigUtils.staticPcResourceUrl(null));
        }
        if(pcLoginUser != null && pcLoginUser.getWeixin() != null) {
            Map<String, String> userParam = Maps.newHashMap();
            userParam.put("userName", pcLoginUser.getWeixin().getWeixinName());
            if(pcLoginUser.getWeixin().getHeadimgUrl() != null) {
                userParam.put("headImage", pcLoginUser.getWeixin().getHeadimgUrl().replace("http:", "https:"));
            }
            userParam.put("signature", pcLoginUser.getSignature());
            mav.addAllObjects(userParam);
        }
        if(pcLoginUser == null || pcLoginUser.getRole() == null) {
            mav.addObject("roleId", 0);
        } else {
            mav.addObject("roleId", pcLoginUser.getRole());
        }
        mav.addObject("feedBack", ConfigUtils.getFeedBackId());
        mav.addObject("isDevelopment", ConfigUtils.isDevelopment());
        return mav;
    }

}
