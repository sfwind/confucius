package com.iquanwai.confucius.web;

import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.resolver.PCLoginUser;
import com.iquanwai.confucius.resolver.PCLoginUserResolver;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.account.websocket.SessionSocketHandler;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by nethunder on 2017/1/3.
 */
@Controller
public class PCIndexController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/pc/static/**")
    public ModelAndView getStatic(HttpServletRequest request){
        return pcView(request);
    }

    /**
     * 前往home页面
     */
    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public ModelAndView getHome(HttpServletRequest request) {
        return pcView(request);
    }

    /**
     * 前往挑战任务修改页面
     */
    @RequestMapping(value = "/fragment/c")
    public ModelAndView getChallenge(HttpServletRequest request) {
        return pcView(request);
    }

    /**
     * 前往挑战任务列表页面
     */
    @RequestMapping(value = "/fragment/c/list")
    public ModelAndView getChallengeList(HttpServletRequest request) {
        return pcView(request);
    }

    /**
     * 前往挑战任务显示页面
     */
    @RequestMapping(value = "/fragment/c/show")
    public ModelAndView showChallenge(HttpServletRequest request) {
        return pcView(request);
    }

    /**
     * 前往登录页面
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView getLoginPage(HttpServletRequest request) {
        return pcView(request);
    }



    private ModelAndView pcView(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("home");
        if(request.getParameter("debug")!=null){
            if(ConfigUtils.isFrontDebug()){
                mav.addObject("resource", "http://0.0.0.0:4000/pc_bundle.js");
            }else{
                mav.addObject("resource", ConfigUtils.staticPcResourceUrl());
            }
        }else{
            mav.addObject("resource", ConfigUtils.staticPcResourceUrl());
        }
        return mav;
    }

}
