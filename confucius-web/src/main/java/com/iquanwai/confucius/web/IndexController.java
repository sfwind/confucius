package com.iquanwai.confucius.web;

import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.util.CookieUtils;
import com.iquanwai.confucius.util.WebUtils;
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

/**
 * Created by justin on 16/9/9.
 */
@Controller
public class IndexController {
    @Autowired
    private OAuthService oAuthService;
    @Autowired
    private AccountService accountService;

    private Logger logger = LoggerFactory.getLogger(IndexController.class);

    @RequestMapping(value = "/static/**",method = RequestMethod.GET)
    public ModelAndView getIndex(HttpServletRequest request) {
        return courseView(request);
    }

    @RequestMapping(value = "/introduction/my",method = RequestMethod.GET)
    public ModelAndView getIntroductionIndex(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String accessToken = CookieUtils.getCookie(request, OAuthService.ACCESS_TOKEN_COOKIE_NAME);
        if(!checkAccessToken(accessToken)){
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            WebUtils.auth(request, response);
            return null;
        }

        return courseView(request);
    }

    @RequestMapping(value = "/pay",method = RequestMethod.GET)
    public ModelAndView getPayIndex(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String accessToken = CookieUtils.getCookie(request, OAuthService.ACCESS_TOKEN_COOKIE_NAME);
        if(!checkAccessToken(accessToken)){
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            WebUtils.auth(request, response);
            return null;
        }
        return courseView(request);
    }

    @RequestMapping(value = "/personal/edit",method = RequestMethod.GET)
    public ModelAndView getPersonalEditIndex(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String accessToken = CookieUtils.getCookie(request, OAuthService.ACCESS_TOKEN_COOKIE_NAME);
        if(!checkAccessToken(accessToken)){
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            WebUtils.auth(request, response);
            return null;
        }
        return courseView(request);
    }

    @RequestMapping(value = "/certificate/**",method = RequestMethod.GET)
    public ModelAndView getCertificateIndex(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String accessToken = CookieUtils.getCookie(request, OAuthService.ACCESS_TOKEN_COOKIE_NAME);
        if(!checkAccessToken(accessToken)){
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            WebUtils.auth(request, response);
            return null;
        }
        return courseView(request);
    }

    private boolean checkAccessToken(String accessToken){
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
}
