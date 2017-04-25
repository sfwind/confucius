package com.iquanwai.confucius.web;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.domain.whitelist.WhiteListService;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    private Logger logger = LoggerFactory.getLogger(getClass());


    @RequestMapping(value = "/static/**",method = RequestMethod.GET)
    public ModelAndView getIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        if(!checkAccessToken(request)){
//            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
//            WebUtils.auth(request, response);
//            return null;
//        }

        return courseView(request);
    }

    @RequestMapping(value = "/introduction/my",method = RequestMethod.GET)
    public ModelAndView getIntroductionIndex(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) throws Exception{
        if(!checkAccessToken(request)){
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            WebUtils.auth(request, response);
            return null;
        }

        if(ConfigUtils.isDevelopment()){
            //如果不在白名单中,直接403报错
            boolean result = whiteListService.isInWhiteList(WhiteList.TEST, loginUser.getOpenId());
            if(!result){
                response.sendRedirect("/403.jsp");
                return null;
            }
        }

        return courseView(request);
    }

    @RequestMapping(value = "/pay/**",method = RequestMethod.GET)
    public ModelAndView getPayIndex(LoginUser loginUser,HttpServletRequest request, HttpServletResponse response) throws Exception{
        if(!checkAccessToken(request)){
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            WebUtils.auth(request, response);
            return null;
        }
        return courseView(request, loginUser);
    }

    @RequestMapping(value = "/personal/edit",method = RequestMethod.GET)
    public ModelAndView getPersonalEditIndex(HttpServletRequest request, HttpServletResponse response) throws Exception{
        if(!checkAccessToken(request)){
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            WebUtils.auth(request, response);
            return null;
        }
        return courseView(request);
    }


    @RequestMapping(value = "/personal/static/**",method = RequestMethod.GET)
    public ModelAndView getPersonalIndex(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) throws Exception{
        if(!checkAccessToken(request)){
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            WebUtils.auth(request, response);
            return null;
        }
        return courseView(request,loginUser);
    }

    @RequestMapping(value = "/operation/static/**",method = RequestMethod.GET)
    public ModelAndView getOperationIndex(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) throws Exception{
        if(!checkAccessToken(request)){
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            WebUtils.auth(request, response);
            return null;
        }
        return courseView(request, loginUser);
    }

    @RequestMapping(value = "/certificate/**",method = RequestMethod.GET)
    public ModelAndView getCertificateIndex(HttpServletRequest request, HttpServletResponse response) throws Exception{
        if(!checkAccessToken(request)){
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            WebUtils.auth(request, response);
            return null;
        }
        return courseView(request);
    }

    private boolean checkAccessToken(HttpServletRequest request){
        if(request.getParameter("debug")!=null && ConfigUtils.isFrontDebug()){
            return true;
        }

        String accessToken = CookieUtils.getCookie(request, OAuthService.ACCESS_TOKEN_COOKIE_NAME);
        String openId = oAuthService.openId(accessToken);

        if(StringUtils.isEmpty(openId)){
            return false;
        }

        Account account = accountService.getAccount(openId, false);

        return account!=null;
    }


    private ModelAndView courseView(HttpServletRequest request){
        ModelAndView mav = new ModelAndView("course");
        if(request.getParameter("debug")!=null){
            if(ConfigUtils.isFrontDebug()){
                mav.addObject("resource", "http://0.0.0.0:4000/bundle.js");
            }else{
                mav.addObject("resource", ConfigUtils.staticResourceUrl());
            }
        }else{
            mav.addObject("resource", ConfigUtils.staticResourceUrl());
        }
        return mav;
    }

    private ModelAndView courseView(HttpServletRequest request, LoginUser loginUser){
        ModelAndView mav = new ModelAndView("course");

        if(request.getParameter("debug")!=null){
            if(ConfigUtils.isFrontDebug()){
                mav.addObject("resource", "http://0.0.0.0:4000/bundle.js");
            }else{
                mav.addObject("resource", ConfigUtils.staticResourceUrl());
            }
        }else{
            mav.addObject("resource", ConfigUtils.staticResourceUrl());
        }

        if (loginUser != null) {
            Map<String, String> userParam = Maps.newHashMap();
            userParam.put("userName", loginUser.getWeixinName());
            userParam.put("headImage",loginUser.getHeadimgUrl());
            mav.addAllObjects(userParam);
        }

        return mav;
    }
}
