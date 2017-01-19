package com.iquanwai.confucius.biz.domain.survey;

import com.iquanwai.confucius.biz.po.survey.SurveyQuestionSubmit;
import com.iquanwai.confucius.biz.po.survey.SurveySubmit;

import java.util.List;

/**
 * Created by nethunder on 2017/1/17.
 */
public interface SurveyService {
    Integer insertSurveySubmit(String openId, Integer activity);

    Boolean submitSurvey(SurveySubmit submit);

    SurveySubmit load(Integer id);

    void batchSubmitQuestions(List<SurveyQuestionSubmit> submits);
}
