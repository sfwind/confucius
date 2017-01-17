package com.iquanwai.confucius.web.pc.survey.controller;

import com.iquanwai.confucius.biz.po.survey.SurveySubmit;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Created by nethunder on 2017/1/17.
 */
@RestController
@RequestMapping("/pc/survey")
public class SurveyController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @RequestMapping(value = "/wjx", method = RequestMethod.GET)
    public void redirectToSurvey(PCLoginUser pcLoginUser,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 @RequestParam("activity") Integer activity) {
        Assert.notNull(pcLoginUser, "用户的登录信息为空");
        // 插入问卷提交表
        Integer submitId = new Double(Double.parseDouble(Math.random()*10000+"")).intValue()+1;

        // 获取url
        String wjxUrl = ConfigUtils.getSurveyUrl(activity);
        if (wjxUrl == null) {
            logger.error("问卷id：{}，没有找到问卷链接", activity);
            try {
                Writer out = response.getWriter();
                out.write("打开问卷链接失败");
            } catch (IOException e) {
                logger.error("打开问卷失败", e);
            }
        } else {
            // 拼接url
            wjxUrl = wjxUrl + "?sojumpparm=" + submitId;
            try {
                WebUtils.wjx(request, response, wjxUrl);
            } catch (Exception e) {
                logger.error("打开问卷星失败", e);
                try {
                    response.sendRedirect("/403.jsp");
                } catch (IOException e1) {
                    // ignore
                }
            }
        }
    }

    @RequestMapping("/wjx/submit")
    public ResponseEntity<Map<String,Object>> wjxSubmit(PCLoginUser loginUser, @RequestBody Map<String,Object> submitObject){
        // 问卷星提交回调接口,数据如下
        /**
         * {"activity":"11769325",
         * "name":"请输入您的标题",
         * "sojumpparm":"122233",
         * "q1":"2",
         * "q2":"1",
         * "index":"7",
         * "timetaken":"47",
         * "submittime":"2017-01-17 10:37:32",
         * "totalvalue":"3"}
         */
        logger.info("User:{}",loginUser);
        logger.info("submitObject:{}", submitObject);
        SurveySubmit surveySubmit = new SurveySubmit();






        return WebUtils.success();
    }

}
