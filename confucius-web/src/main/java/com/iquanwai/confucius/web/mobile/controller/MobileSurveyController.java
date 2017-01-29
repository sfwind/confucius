package com.iquanwai.confucius.web.mobile.controller;

import com.iquanwai.confucius.biz.domain.survey.SurveyService;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by nethunder on 2017/1/29.
 */
@RestController
@RequestMapping("/survey")
public class MobileSurveyController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SurveyService surveyService;

    @RequestMapping(value = "/wjx", method = RequestMethod.GET)
    public void redirectToSurvey(LoginUser loginUser,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 @RequestParam("activity") Integer activity) {
        try {
            Assert.notNull(loginUser, "用户的登录信息不能为空");
            // 插入问卷提交表
            Integer submitId = surveyService.insertSurveySubmit(loginUser.getOpenId(), activity);
            if (submitId != null && submitId > 0) {
                // 获取url
                String wjxUrl = ConfigUtils.getSurveyUrl(activity);
                if (wjxUrl == null) {
                    logger.error("问卷id：{}，没有找到问卷链接", activity);
                    response.sendRedirect("/403.jsp");
                } else {
                    // 拼接url
                    wjxUrl = wjxUrl + "?sojumpparm=" + submitId;
                    WebUtils.wjx(request, response, wjxUrl);
                }
            } else {
                logger.error("用户:{},插入问卷:{},提交记录失败", loginUser.getOpenId(), activity);
                response.sendRedirect("/403.jsp");
            }
        } catch (Exception e) {
            logger.error("跳转到问卷页面失败",e);
            try{
                response.sendRedirect("/403.jsp");
            } catch (Exception e1){
                logger.error("跳转异常页面失败", e);
            }
        }

    }
}
