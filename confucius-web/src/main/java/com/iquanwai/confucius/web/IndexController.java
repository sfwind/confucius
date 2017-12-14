package com.iquanwai.confucius.web;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.exception.NotFollowingException;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.OperationLog;
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

    private static final String COURSE_VIEW = "course";
    private static final String PAY_VIEW = "pay";

    private Logger logger = LoggerFactory.getLogger(getClass());


    @RequestMapping(value = "/static/**", method = RequestMethod.GET)
    public ModelAndView getIndex(HttpServletRequest request) throws Exception {
        return courseView(request);
    }

    @RequestMapping(value = "/pay/static/**", method = RequestMethod.GET)
    public ModelAndView getPayStaticIndex(HttpServletRequest request) throws Exception {
        OperationLog operationLog = new OperationLog().function("打点").module("访问页面").action("游客访问")
                .memo(request.getRequestURI());
        operationLogService.log(operationLog);
        return courseView(request, null, PAY_VIEW);
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
                response.sendRedirect("/static/subscribe");
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

    @RequestMapping(value = "/certificate/**",method = RequestMethod.GET)
    public ModelAndView getCertificateIndex(HttpServletRequest request, HttpServletResponse response) throws Exception{
        if(!checkAccessToken(request,response)){
            return null;
        }
        return courseView(request);
    }


    @RequestMapping(value = "/heartbeat", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> heartbeat() throws Exception {
        return WebUtils.success();
    }

    private ModelAndView courseView(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("course");
        String domainName = request.getHeader("Domain-Name");
        mav.addObject("resource", ConfigUtils.staticResourceUrl(domainName));
        return mav;
    }

    private ModelAndView courseView(HttpServletRequest request, LoginUser loginUser, String viewName) {
        ModelAndView mav = new ModelAndView(viewName);
        String resource;
        String domainName = request.getHeader("Domain-Name");

        switch (viewName) {
            case COURSE_VIEW: {
                resource = ConfigUtils.staticResourceUrl(domainName);
            }
            break;
            case PAY_VIEW: {
                resource = ConfigUtils.staticPayUrl(domainName);
            }
            break;
            default:
                resource = ConfigUtils.staticResourceUrl(domainName);
        }

        mav.addObject("resource", resource);

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
