package com.iquanwai.confucius.web;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.domain.whitelist.WhiteListService;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.WhiteList;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.ua.UAUtils;
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

    private Logger logger = LoggerFactory.getLogger(getClass());


    @RequestMapping(value = "/static/**",method = RequestMethod.GET)
    public ModelAndView getIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        if(!checkAccessToken(request,response)){
//            return null;
//        }
        return courseView(request);
    }

    @RequestMapping(value = "/introduction/my",method = RequestMethod.GET)
    public ModelAndView getIntroductionIndex(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) throws Exception{
        if(!checkAccessToken(request,response)){
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

    @RequestMapping(value = "/pay/pay",method = RequestMethod.GET)
    public ModelAndView getPayPayIndex(LoginUser loginUser,HttpServletRequest request, HttpServletResponse response) throws Exception{
        if(!checkAccessToken(request,response)){
            return null;
        }
        String ua = request.getHeader("user-agent");
        //TODO 可以写到配置里
        if (UAUtils.isLowerAndroid(ua, 4, 4)) {
            logger.error("openid:{},nickname;{},安卓版本过低，进入简化的付款页面", loginUser == null ? null : loginUser.getOpenId(), loginUser == null ? null : loginUser.getWeixinName());
            response.sendRedirect("/pay/simple");
            return null;
        }
        return courseView(request, loginUser);
    }


    @RequestMapping(value = "/pay/**",method = RequestMethod.GET)
    public ModelAndView getPayIndex(LoginUser loginUser,HttpServletRequest request, HttpServletResponse response) throws Exception{
        if(!checkAccessToken(request,response)){
            return null;
        }
        return courseView(request, loginUser);
    }

    @RequestMapping(value = "/personal/edit",method = RequestMethod.GET)
    public ModelAndView getPersonalEditIndex(HttpServletRequest request, HttpServletResponse response) throws Exception{
        if(!checkAccessToken(request,response)){
            return null;
        }
        return courseView(request);
    }


    @RequestMapping(value = "/personal/static/**",method = RequestMethod.GET)
    public ModelAndView getPersonalIndex(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) throws Exception{
        if(!checkAccessToken(request,response)){
            return null;
        }
        return courseView(request,loginUser);
    }

    @RequestMapping(value = "/operation/static/**",method = RequestMethod.GET)
    public ModelAndView getOperationIndex(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) throws Exception{
        if(!checkAccessToken(request,response)){
            return null;
        }
        return courseView(request, loginUser);
    }

    @RequestMapping(value = "/certificate/**",method = RequestMethod.GET)
    public ModelAndView getCertificateIndex(HttpServletRequest request, HttpServletResponse response) throws Exception{
        if(!checkAccessToken(request,response)){
            return null;
        }
        return courseView(request);
    }

    private boolean checkAccessToken(HttpServletRequest request,HttpServletResponse response){
        if(request.getParameter("debug")!=null && ConfigUtils.isFrontDebug()){
            return true;
        }

        String accessToken = CookieUtils.getCookie(request, OAuthService.ACCESS_TOKEN_COOKIE_NAME);
        String openId = oAuthService.openId(accessToken);

        if(StringUtils.isEmpty(openId)){
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            try {
                WebUtils.auth(request, response);
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
            return false;
        }

        Account account = accountService.getAccount(openId, false);
        logger.info("用户信息, {}", account);
        if (account != null) {
            if (account.getSubscribe() != null && account.getSubscribe() == 0) {
                logger.info("用户未关注, {}", account);
                // 未关注
                try {
                    response.sendRedirect(ConfigUtils.adapterDomainName() + "/static/subscribe");
                } catch (IOException e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
                return false;
            }
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
            if(loginUser.getHeadimgUrl()!=null){
                userParam.put("headImage",loginUser.getHeadimgUrl().replace("http:","https:"));
            }
            mav.addAllObjects(userParam);
        }

        return mav;
    }
}
