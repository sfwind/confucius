package com.iquanwai.confucius.web;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.domain.whitelist.WhiteListService;
import com.iquanwai.confucius.biz.exception.NotFollowingException;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.WhiteList;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.util.CookieUtils;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created by justin on 16/9/9.
 */
@Controller
public class IndexController {
    @Autowired
    private OAuthService oAuthService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private WhiteListService whiteListService;

    private static final String COURSE_VIEW = "course";
    private static final String PAY_VIEW = "pay";

    private Logger logger = LoggerFactory.getLogger(getClass());


    @RequestMapping(value = "/static/**", method = RequestMethod.GET)
    public ModelAndView getIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        if(!checkAccessToken(request,response)){
//            return null;
//        }
        return courseView(request);
    }

    @RequestMapping(value = "/pay/pay", method = RequestMethod.GET)
    public ModelAndView getPayPayIndex(LoginUser loginUser, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!checkAccessToken(request, response)) {
            return null;
        }
        if (ConfigUtils.payPrePublish()) {
            // 测试状态
            boolean inWhite = whiteListService.isInWhiteList(WhiteList.PAY_TEST, loginUser.getId());
            if (!inWhite) {
                response.sendRedirect("/403.jsp");
                return null;
            }
        }
        return courseView(request, loginUser, PAY_VIEW);
    }


    @RequestMapping(value = "/pay/**", method = RequestMethod.GET)
    public ModelAndView getPayIndex(LoginUser loginUser, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!checkAccessToken(request, response)) {
            return null;
        }
        return courseView(request, loginUser, PAY_VIEW);
    }

    private boolean checkAccessToken(HttpServletRequest request, HttpServletResponse response) {
        if (request.getParameter("debug") != null && ConfigUtils.isFrontDebug()) {
            return true;
        }

        String accessToken = CookieUtils.getCookie(request, OAuthService.ACCESS_TOKEN_COOKIE_NAME);
        String openId = oAuthService.openId(accessToken);

        if (StringUtils.isEmpty(openId)) {
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            try {
                WebUtils.auth(request, response);
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
            return false;
        }

        Account account = null;
        try {
            account = accountService.getAccount(openId, false);
        } catch (NotFollowingException e) {
            try {
                response.sendRedirect(ConfigUtils.adapterDomainName() + "/static/subscribe");
                return false;
            } catch (IOException e1) {
                logger.error(e1.getLocalizedMessage(), e1);
            }
        }
        if (account != null) {
            return true;
        } else {
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            try {
                WebUtils.auth(request, response);
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
            return false;
        }
    }

    @RequestMapping(value = "/heartbeat", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> heartbeat() throws Exception {
        return WebUtils.success();
    }

    private ModelAndView courseView(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("course");
        if (request.getParameter("debug") != null) {
            if (ConfigUtils.isFrontDebug()) {
                mav.addObject("resource", "http://0.0.0.0:4000/bundle.js");
            } else {
                mav.addObject("resource", ConfigUtils.staticResourceUrl());
            }
        } else {
            mav.addObject("resource", ConfigUtils.staticResourceUrl());
        }
        return mav;
    }

    private ModelAndView courseView(HttpServletRequest request, LoginUser loginUser, String viewName) {
        ModelAndView mav = new ModelAndView(viewName);
        String testUrl = "";
        String resource = "";
        switch (viewName) {
            case COURSE_VIEW: {
                testUrl = "http://0.0.0.0:4000/bundle.js";
                resource = ConfigUtils.staticResourceUrl();
            }
            break;
            case PAY_VIEW: {
                testUrl = "http://0.0.0.0:4000/pay_bundle.js";
                resource = ConfigUtils.staticPayUrl();
            }
            break;
        }


        if (request.getParameter("debug") != null) {
            if (ConfigUtils.isFrontDebug()) {
                mav.addObject("resource", testUrl);
            } else {
                mav.addObject("resource", resource);
            }
        } else {
            mav.addObject("resource", resource);
        }

        if (loginUser != null) {
            Map<String, String> userParam = Maps.newHashMap();
            userParam.put("userName", loginUser.getWeixinName());
            if (loginUser.getHeadimgUrl() != null) {
                userParam.put("headImage", loginUser.getHeadimgUrl().replace("http:", "https:"));
            }
            mav.addAllObjects(userParam);
        }

        return mav;
    }
}
