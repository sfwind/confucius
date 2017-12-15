package com.iquanwai.confucius.web.mobile.survey;

import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.survey.SurveyService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
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
import java.net.URLEncoder;

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
                                 @RequestParam("activity") Integer activity) throws IOException {
        if (loginUser == null) {
            response.sendRedirect(ConfigUtils.adapterDomainName() +
                    "/static/login/result?err=" +
                    URLEncoder.encode("您还未关注公众号", "UTF-8"));
        } else {
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("问卷")
                    .function("问卷调查")
                    .action("手机端打开问卷")
                    .memo(activity + "");
            operationLogService.log(operationLog);
            // 插入问卷提交表
            try {
                String redirectUrl = surveyService.getRedirectUrl(loginUser.getOpenId(), activity);
                WebUtils.wjx(request, response, redirectUrl);
            } catch (Exception e) {
                logger.error("跳转问卷星失败:", e);
                response.sendRedirect("/403.jsp");

            }
        }
    }
}
