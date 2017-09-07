package com.iquanwai.confucius.biz.domain.survey;

import com.iquanwai.confucius.biz.dao.common.survey.SurveyHrefDao;
import com.iquanwai.confucius.biz.dao.common.survey.SurveyQuestionSubmitDao;
import com.iquanwai.confucius.biz.dao.common.survey.SurveySubmitDao;
import com.iquanwai.confucius.biz.po.common.survey.SurveyHref;
import com.iquanwai.confucius.biz.po.common.survey.SurveyQuestionSubmit;
import com.iquanwai.confucius.biz.po.common.survey.SurveySubmit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    private SurveyHrefDao surveyHrefDao;

    @Override
    public Integer insertSurveySubmit(String openId, Integer activity) {
        return surveySubmitDao.insert(openId, activity);
    }

    @Override
    public Boolean submitSurvey(SurveySubmit surveySubmit) {
        // 先查询该提交id是否是该用户的
        logger.info("问卷:{} 提交", surveySubmit.getId());
        logger.debug("问卷信息:{}", surveySubmit);
        return surveySubmitDao.submit(surveySubmit);
    }

    @Override
    public SurveySubmit load(Integer id) {
        return surveySubmitDao.load(SurveySubmit.class, id);
    }

    @Override
    public void batchSubmitQuestions(List<SurveyQuestionSubmit> submits) {
        surveyQuestionSubmitDao.batchInsert(submits);
    }

    @Override
    public SurveyHref loadSurveyHref(Integer activity) {
        SurveyHref surveyHref = surveyHrefDao.loadSurveyHref(activity);
        if (surveyHref != null && surveyHref.getActivity() != null) {
            surveyHref.setMobileHref(MOBILE_PREFIX.replace("{activity}", surveyHref.getActivity().toString()));
            surveyHref.setPcHref(PC_PREFIX.replace("{activity}", surveyHref.getActivity().toString()));
        }
        return surveyHref;
    }

    @Override
    public String getRedirectUrl(String openId, Integer activity) {
        // 插入问卷提交表
        SurveyHref surveyHref = this.loadSurveyHref(activity);
        Assert.notNull(surveyHref, "没有找到问卷链接:" + activity);
        Integer submitId = this.insertSurveySubmit(openId, activity);
        Assert.isTrue(submitId != -1, "用户:" + openId + "插入问卷 " + activity + " 失败");
        // 拼接url
        String wjxUrl = surveyHref.getRealHref();
        wjxUrl = wjxUrl + "?sojumpparm=" + submitId;
        return wjxUrl;
    }

    @Override
    public List<SurveyHref> loadAllSurveyHref(){
        return surveyHrefDao.loadAll(SurveyHref.class).stream().filter(item -> !item.getDel()).collect(Collectors.toList());
    }

    @Override
    public Boolean updateSurveyHref(SurveyHref href){
        return surveyHrefDao.updateSurveyHref(href) > 0;
    }

    @Override
    public Boolean deleteSurveyHref(Integer id){
        return surveyHrefDao.deleteSurveyHref(id) > 0;
    }
}
