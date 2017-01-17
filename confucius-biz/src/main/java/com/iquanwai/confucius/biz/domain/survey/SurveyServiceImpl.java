package com.iquanwai.confucius.biz.domain.survey;

import com.iquanwai.confucius.biz.dao.survey.SurveySubmitDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by nethunder on 2017/1/17.
 */
@Service
public class SurveyServiceImpl implements SurveyService {
    @Autowired
    private SurveySubmitDao surveySubmitDao;

    @Override
    public Integer insert(String openId,Integer activity) {
        return surveySubmitDao.insert(openId, activity);
    }
}
