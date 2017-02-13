package com.iquanwai.confucius.biz.domain.survey;

import com.iquanwai.confucius.biz.dao.survey.SurveyQuestionSubmitDao;
import com.iquanwai.confucius.biz.dao.survey.SurveySubmitDao;
import com.iquanwai.confucius.biz.po.survey.SurveyQuestionSubmit;
import com.iquanwai.confucius.biz.po.survey.SurveySubmit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by nethunder on 2017/1/17.
 */
@Service
public class SurveyServiceImpl implements SurveyService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private SurveySubmitDao surveySubmitDao;
    @Autowired
    private SurveyQuestionSubmitDao surveyQuestionSubmitDao;

    @Override
    public Integer insertSurveySubmit(String openId, Integer activity) {
        return surveySubmitDao.insert(openId, activity);
    }

    @Override
    public Boolean submitSurvey(SurveySubmit surveySubmit){
        // 先查询该提交id是否是该用户的
        logger.info("问卷:{} 提交",surveySubmit.getId());
        logger.debug("问卷信息:{}", surveySubmit);
        return surveySubmitDao.submit(surveySubmit);
    }

    @Override
    public SurveySubmit load(Integer id){
        return surveySubmitDao.load(SurveySubmit.class, id);
    }

    @Override
    public void batchSubmitQuestions(List<SurveyQuestionSubmit> submits) {
        surveyQuestionSubmitDao.batchInsert(submits);
    }

}
