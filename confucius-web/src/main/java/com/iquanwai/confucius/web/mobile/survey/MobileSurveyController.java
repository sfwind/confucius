package com.iquanwai.confucius.web.mobile.survey;

import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.survey.SurveyService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.web.resolver.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by nethunder on 2017/1/29.
 */
@RestController
@RequestMapping("/survey")
public class MobileSurveyController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SurveyService surveyService;
    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping(value = "/wjx", method = RequestMethod.GET)
    public void redirectToSurvey(LoginUser loginUser,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 @RequestParam("activity") String activity) {
        if (loginUser == null) {
            try {
                response.sendRedirect("/subscribe");
            } catch (Exception e) {
                logger.error("跳转关注页面失败", e);
            }
        } else {
            // 插入问卷提交表
            try {
                OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                        .module("问卷")
                        .function("问卷调查")
                        .action("手机端打开问卷")
                        .memo(activity);
                Integer activityNum = Integer.parseInt(activity);
                operationLogService.log(operationLog);
                String redirectUrl = surveyService.getRedirectUrl(loginUser.getOpenId(), activityNum);
                response.sendRedirect(redirectUrl);
            } catch (NumberFormatException e) {
                logger.error("问卷参数错误{}", activity);
                try {
                    response.sendRedirect("/404.jsp");
                } catch (Exception e1) {
                    logger.error("跳转异常页面失败", e);
                }
            } catch (IOException e) {
                logger.error("跳转问卷星失败:", e);
                try {
                    response.sendRedirect("/403.jsp");
                } catch (Exception e1) {
                    logger.error("跳转异常页面失败", e);
                }
            }
        }
    }
}
