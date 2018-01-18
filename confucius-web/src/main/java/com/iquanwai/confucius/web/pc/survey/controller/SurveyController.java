package com.iquanwai.confucius.web.pc.survey.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.survey.SurveyService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.common.survey.SurveyQuestionSubmit;
import com.iquanwai.confucius.biz.po.common.survey.SurveySubmit;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.web.pc.survey.dto.SurveyResultDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * Created by nethunder on 2017/1/17.
 */
@Controller
@RequestMapping("/pc/survey")
public class SurveyController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SurveyService surveyService;
    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping(value = "/wjx", method = RequestMethod.GET)
    public void redirectToSurvey(PCLoginUser pcLoginUser,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 @RequestParam("activity") String activity) {
        try {
            Assert.notNull(pcLoginUser, "用户的登录信息不能为空");
            OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                    .module("问卷")
                    .function("问卷调查")
                    .action("PC端打开问卷")
                    .memo(activity);
            Integer activityNum = Integer.parseInt(activity);
            operationLogService.log(operationLog);
            String redirectUrl = surveyService.getRedirectUrl(pcLoginUser.getOpenId(), activityNum);
            response.sendRedirect(redirectUrl);
        } catch (NumberFormatException e) {
            logger.error("问卷参数错误{}", activity);
            try {
                response.sendRedirect("/404.jsp");
            } catch (Exception e1) {
                logger.error("跳转异常页面失败", e);
            }
        } catch (Exception e) {
            logger.error("跳转到问卷页面失败", e);
            try {
                response.sendRedirect("/403.jsp");
            } catch (Exception e1) {
                logger.error("跳转异常页面失败", e);
            }
        }

    }

    @RequestMapping("/wjx/submit")
    public ResponseEntity<Map<String, Object>> wjxSubmit(@RequestBody Map<String, Object> submitObject) {
        Assert.notNull(submitObject, "提交数据不能为空");
        SurveyResultDto surveyResultDto = new SurveyResultDto();
        try {
            BeanUtils.populate(surveyResultDto, submitObject);
            SurveySubmit surveySubmit = this.buildSurveySubmit(surveyResultDto);
            SurveySubmit oldSubmit = surveyService.load(surveySubmit.getId());
            if (oldSubmit == null) {
                logger.error("提交问卷数据异常:{}", submitObject);
                return WebUtils.error("提交问卷数据异常");
            }
            Boolean updateStatus = surveyService.submitSurvey(surveySubmit);
            if (updateStatus) {
                List<SurveyQuestionSubmit> questionSubmits = this.buildQuestionSubmits(oldSubmit, submitObject);
                surveyService.batchSubmitQuestions(questionSubmits);
            } else {
                // 提交失败
                logger.error("提交问卷数据异常,插入问题异常:{}", submitObject);
                return WebUtils.error("提交问卷数据异常,插入问题异常");
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("接收问卷提交数据失败", e);
            return WebUtils.error(e.getLocalizedMessage());
        }
        return WebUtils.success();
    }

    private List<SurveyQuestionSubmit> buildQuestionSubmits(SurveySubmit oldSubmit, Map<String, Object> submitObject) {
        // 先取出之前生成的数据
        List<SurveyQuestionSubmit> surveyQuestionSubmits = Lists.newArrayList();
        for (Map.Entry<String, Object> entry : submitObject.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if ('q' == key.charAt(0)) {
                // 是问题
                try {
                    SurveyQuestionSubmit surveyQuestionSubmit = new SurveyQuestionSubmit();
                    surveyQuestionSubmit.setOpenId(oldSubmit.getOpenId());
                    surveyQuestionSubmit.setQuestionLabel(key);
                    surveyQuestionSubmit.setActivity(oldSubmit.getActivity());
                    surveyQuestionSubmit.setContent(value.toString());
                    surveyQuestionSubmit.setSurveySubmitId(oldSubmit.getId());
                    surveyQuestionSubmits.add(surveyQuestionSubmit);
                } catch (Exception e) {
                    // 解析信息失败
                    logger.error("解析失败", e);
                }
            }
        }
        return surveyQuestionSubmits;
    }

    private SurveySubmit buildSurveySubmit(SurveyResultDto dto) {
        SurveySubmit surveySubmit = new SurveySubmit();
        surveySubmit.setId(dto.getSojumpparm());
        surveySubmit.setTotalValue(dto.getTotalvalue());
        surveySubmit.setSubmitTime(DateUtils.parseStringToDateTime(dto.getSubmittime()));
        surveySubmit.setTimeTaken(dto.getTimetaken());
        surveySubmit.setActivity(dto.getActivity());
        surveySubmit.setSequence(dto.getIndex());
        return surveySubmit;
    }

}
