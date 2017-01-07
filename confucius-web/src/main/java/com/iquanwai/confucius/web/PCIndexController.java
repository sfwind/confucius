package com.iquanwai.confucius.web;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.resolver.PCLoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by nethunder on 2017/1/3.
 */
@Controller
public class PCIndexController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/pc/static/**")
    public ModelAndView getStatic(HttpServletRequest request,PCLoginUser pcLoginUser){
        return pcView(request,pcLoginUser);
    }

    /**
     * 前往碎片化页面
     * @param request
     * @return
     */
    @RequestMapping(value = "/fragment/**")
    public ModelAndView getFragmentPage(HttpServletRequest request,PCLoginUser pcLoginUser) {
        return pcView(request,pcLoginUser);
    }
    /**
     * 前往home页面
     */
    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public ModelAndView getHome(HttpServletRequest request,PCLoginUser pcLoginUser) {
            return pcView(request,pcLoginUser);
    }
    @RequestMapping(value="/servercode")
    public ModelAndView getServerCodePage(HttpServletRequest request,PCLoginUser pcLoginUser) {
        return pcView(request,pcLoginUser);
    }

//    /**
//     * 前往挑战任务修改页面
//     */
//    @RequestMapping(value = "/fragment/c")
//    public ModelAndView getChallenge(HttpServletRequest request) {
//        return pcView(request);
//    }
//
//    /**
//     * 前往挑战任务列表页面
//     */
//    @RequestMapping(value = "/fragment/c/list")
//    public ModelAndView getChallengeList(HttpServletRequest request) {
//        return pcView(request);
//    }
//
//    /**
//     * 前往挑战任务显示页面
//     */
//    @RequestMapping(value = "/fragment/c/show")
//    public ModelAndView showChallenge(HttpServletRequest request) {
//        return pcView(request);
//    }

    /**
     * 前往登录页面
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView getLoginPage(HttpServletRequest request,PCLoginUser pcLoginUser) {
        return pcView(request,pcLoginUser);
    }



    private ModelAndView pcView(HttpServletRequest request,PCLoginUser pcLoginUser) {
        ModelAndView mav = new ModelAndView("home");
        if(request.getParameter("debug")!=null){
            if(ConfigUtils.isFrontDebug()){
                mav.addObject("resource", "http://0.0.0.0:4000/pc_bundle.js");
                mav.addObject("loginSocketUrl","127.0.0.1:8080/session");
            }else{
                mav.addObject("resource", ConfigUtils.staticPcResourceUrl());
                mav.addObject("loginSocketUrl",ConfigUtils.getLoginSocketUrl());
            }
        }else{
            mav.addObject("resource", ConfigUtils.staticPcResourceUrl());
            mav.addObject("loginSocketUrl",ConfigUtils.getLoginSocketUrl());
        }
        if (pcLoginUser != null && pcLoginUser.getWeixin() != null) {
            Map<String, String> userParam = Maps.newHashMap();
            userParam.put("userName", pcLoginUser.getWeixin().getWeixinName());
            userParam.put("headImage",pcLoginUser.getWeixin().getHeadimgUrl());
            mav.addAllObjects(userParam);
        }
        return mav;
    }

}
