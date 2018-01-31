package com.iquanwai.confucius.web;

import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQPublisher;
import com.iquanwai.confucius.mq.CustomerLogoutReceiver;
import com.iquanwai.confucius.web.resolver.UnionUser;
import com.iquanwai.confucius.web.resolver.UnionUserService;
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

@Controller
public class PCIndexController {

    @Autowired
    private UnionUserService unionUserService;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    private RabbitMQPublisher logoutPublisher;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void init() {
        logoutPublisher = rabbitMQFactory.initFanoutPublisher(CustomerLogoutReceiver.TOPIC);
    }

    /**
     * 前往home页面
     */
    @RequestMapping(value = {"/home", "/"}, method = RequestMethod.GET)
    public ModelAndView getHome(HttpServletRequest request, HttpServletResponse response) {
        return ConfigUtils.isDevelopment() ? new ModelAndView("index") : new ModelAndView("home");
    }

    /**
     * 点击圈外同学菜单后，确定需要跳转到的位置
     */
    @RequestMapping(value = "/community", method = RequestMethod.GET)
    public void fragmentGoWhere(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.sendRedirect("/fragment/rise");
    }

    /**
     * 前往 pc 页面，必须存在服务号信息
     */
    @RequestMapping(value = {"/fragment/**", "/backend/**", "/asst/**"})
    public ModelAndView getFragmentPage(HttpServletRequest request, HttpServletResponse response) {
        Callback callback = unionUserService.getCallbackByRequest(request);
        if (callback != null) {
            UnionUser unionUser = unionUserService.getUnionUserByCallback(callback);
            if (unionUser != null && unionUser.getOpenId() == null) {
                try {
                    logger.info("用户尚未有服务号信息，跳转二维码关注页面");
                    response.sendRedirect("/servercode");
                } catch (IOException e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            }
        }

        return pcView(request, response);
    }

    /**
     * 前往登录页面
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView getLoginPage(HttpServletRequest request, HttpServletResponse response) {
        return pcView(request, response);
    }

    /**
     * 用户登出
     * 1、删除本地 cookie 数据
     * 2、MQ 发送广播，删除其余节点上的 cookie 数据
     */
    @RequestMapping(value = "/logout")
    public void getLogoutPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 获取当前链接 cookie 的值，作为删除 cookieMap 的 key 值
        String cookie = CookieUtils.getCookie(request, OAuthService.PC_STATE_COOKIE_NAME);
        logger.info("cookie:" + cookie);
        if (cookie == null) {
            response.sendRedirect("/login");
            return;
        }
        // 1、删除 cookie
        unionUserService.removeCookie(UnionUser.Platform.PC, response);
        try {
            // 2、通过 MQ 发送广播，删除每个节点上的 cookie 数据
            logoutPublisher.publish(cookie);
        } catch (ConnectException e) {
            logger.error(e.getLocalizedMessage());
        }
        response.sendRedirect("/login");
    }

    @RequestMapping(value = "/servercode")
    public ModelAndView getServerCodePage(HttpServletRequest request, HttpServletResponse response) {
        return pcView(request, response);
    }

    private ModelAndView pcView(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView("site");
        String domainName = request.getHeader("Host-Test");
        if (request.getParameter("debug") != null) {
            if (ConfigUtils.isFrontDebug()) {
                mav.addObject("resource", "http://0.0.0.0:4000/pc_bundle.js");
                mav.addObject("loginSocketUrl", "127.0.0.1:8080/session");
            } else {
                mav.addObject("resource", ConfigUtils.staticPcResourceUrl(domainName));
            }
        } else {
            mav.addObject("resource", ConfigUtils.staticPcResourceUrl(domainName));
        }
        mav.addObject("isDevelopment", ConfigUtils.isDevelopment());
        mav.addObject("feedBack", ConfigUtils.getFeedBackId());
        return mav;
    }

}
