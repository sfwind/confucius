package com.iquanwai.confucius.web.pc.survey.controller;

import com.iquanwai.confucius.biz.domain.survey.SurveyService;
import com.iquanwai.confucius.biz.po.survey.SurveySubmit;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by nethunder on 2017/1/17.
 */
@RestController
@RequestMapping("/pc/survey")
public class SurveyController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SurveyService surveyService;

    @RequestMapping(value = "/wjx", method = RequestMethod.GET)
    public void redirectToSurvey(PCLoginUser pcLoginUser,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 @RequestParam("activity") Integer activity) {
        try {
            Assert.notNull(pcLoginUser, "用户的登录信息不能为空");
            // 插入问卷提交表
            Integer submitId = surveyService.insert(pcLoginUser.getOpenId(), activity);
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
                logger.error("用户:{},插入问卷:{},提交记录失败", pcLoginUser.getOpenId(), activity);
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

    @RequestMapping("/wjx/submit")
    public ResponseEntity<Map<String, Object>> wjxSubmit(PCLoginUser loginUser, @RequestBody Map<String, Object> submitObject) {
        // 问卷星提交回调接口,数据如下
        /**
         * {activity=11853325,
         * name=全测试1,
         * sojumpparm=4,
         * q1=1, q2=1,2,
         * q3=填空, q4_1=姓名, q4_2=21, q4_3=test, q5=2, q6=2,3,
         * index=2,
         * timetaken=19,
         * submittime=2017-01-17 19:26:01}


         */
        logger.info("User:{}", loginUser);
        logger.info("submitObject:{}", submitObject);
        SurveySubmit surveySubmit = new SurveySubmit();


        return WebUtils.success();
    }

}
