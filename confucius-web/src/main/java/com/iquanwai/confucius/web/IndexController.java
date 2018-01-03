package com.iquanwai.confucius.web;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.subscribe.SubscribeRouterService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.exception.ErrorConstants;
import com.iquanwai.confucius.biz.exception.NotFollowingException;
import com.iquanwai.confucius.biz.exception.WeixinException;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.common.customer.SubscribeRouterConfig;
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
    private OperationLogService operationLogService;
    @Autowired
    private SubscribeRouterService subscribeRouterService;

    private static final String SUBSCRIBE_URL = "/subscribe";

    private static final String PAY_VIEW = "pay";

    private static final String PAY_CAMP = "/pay/camp";

    private static final String PAY_GUEST_CAMP = "/pay/static/camp";
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 点击圈外同学菜单后，确定需要跳转到的位置
     */
    @RequestMapping(value = "/community", method = RequestMethod.GET)
    public void fragmentGoWhere(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.sendRedirect("/fragment/rise");
    }

    @RequestMapping(value = "/subscribe")
    public ModelAndView goSubscribe(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("用户未关注，跳转关注页面：{}", request.getRequestURI());
        return payView(request, null, PAY_VIEW);
    }

    @RequestMapping(value = "/pay/redirect/camp/pay", method = RequestMethod.GET)
    public void getGuestPayCampPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            if (checkFollow(request, response)) {
                // 关注
                response.sendRedirect(PAY_CAMP);
            } else {
                // 未关注
                response.sendRedirect(PAY_GUEST_CAMP);
            }
        } catch (WeixinException e) {
            // ignore WeixinException
            logger.error("微信 Exception");
        }
    }

    @RequestMapping(value = "/pay/static/**", method = RequestMethod.GET)
    public ModelAndView getPayStaticIndex(HttpServletRequest request) throws Exception {
        OperationLog operationLog = new OperationLog().function("打点").module("访问页面").action("游客访问")
                .memo(request.getRequestURI());
        operationLogService.log(operationLog);
        return payView(request, null, PAY_VIEW);
    }

    @RequestMapping(value = "/pay/**", method = RequestMethod.GET)
    public ModelAndView getPayIndex(LoginUser loginUser, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!checkAccessToken(request, response)) {
            return null;
        }
        return payView(request, loginUser, PAY_VIEW);
    }

    private boolean checkFollow(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
            throw new WeixinException(ErrorConstants.ACCESS_TOKEN_INVALID, "cookie无效");
        }

        Account account = null;
        try {
            account = accountService.getAccount(openId, false);
        } catch (NotFollowingException e) {
            return false;
        }
        if (account != null) {
            return true;
        } else {
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            return false;
        }
    }

    private boolean checkAccessToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
                String subscribeKey = request.getParameter("_ws");
                SubscribeRouterConfig subscribeRouterConfig = subscribeRouterService.loadUnSubscribeRouterConfig(request.getRequestURI(), subscribeKey);
                if (subscribeRouterConfig != null) {
                    // 未关注
                    response.sendRedirect(SUBSCRIBE_URL + "?scene=" + subscribeRouterConfig.getScene());
                    return false;
                } else {
                    response.sendRedirect(SUBSCRIBE_URL);
                    return false;
                }
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


    private ModelAndView payView(HttpServletRequest request, LoginUser loginUser, String viewName) {
        ModelAndView mav = new ModelAndView(viewName);
        String domainName = request.getHeader("Host-Test");
        String resource = ConfigUtils.staticPayUrl(domainName);

        if (request.getParameter("debug") != null) {
            if (ConfigUtils.isFrontDebug()) {
                mav.addObject("resource", "http://0.0.0.0:4000/pay_bundle.js");
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
