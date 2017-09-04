package com.iquanwai.confucius.biz.domain.survey;

import com.iquanwai.confucius.biz.po.common.survey.SurveyHref;
import com.iquanwai.confucius.biz.po.common.survey.SurveyQuestionSubmit;
import com.iquanwai.confucius.biz.po.common.survey.SurveySubmit;

import java.util.List;

/**
 * Created by nethunder on 2017/1/17.
 */
public interface SurveyService {

    String PC_PREFIX = "https://www.iquanwai.com/pc/survey/wjx?activity=";
    String MOBILE_PREFIX = "https://www.iquanwai.com/survey/wjx?activity=";

    /**
     * 插入问卷链接
     */
    Integer insertSurveySubmit(String openId, Integer activity);

    /**
     * 提交问卷
     * @param submit 问卷提交数据
     * @return 提交结果
     */
    Boolean submitSurvey(SurveySubmit submit);

    /**
     * 加载问卷
     * @param id submitId
     */
    SurveySubmit load(Integer id);

    /**
     * 批量插入问题
     * @param submits 提交内容
     */
    void batchSubmitQuestions(List<SurveyQuestionSubmit> submits);

    /**
     * 加载问卷链接
     * @param activity 问卷id
     */
    SurveyHref loadSurveyHref(Integer activity);

    /**
     * 获取重定向地址
     */
    String getRedirectUrl(String openId, Integer activity);
}
