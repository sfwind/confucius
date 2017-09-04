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

    Integer insertSurveySubmit(String openId, Integer activity);

    Boolean submitSurvey(SurveySubmit submit);

    SurveySubmit load(Integer id);

    void batchSubmitQuestions(List<SurveyQuestionSubmit> submits);

    SurveyHref loadSurveyHref(Integer activity);

    String getRedirectUrl(String openId, Integer activity);
}
