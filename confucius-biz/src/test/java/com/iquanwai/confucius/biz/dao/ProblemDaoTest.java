package com.iquanwai.confucius.biz.dao;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.dao.fragmentation.HomeworkVoteDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ProblemDao;
import com.iquanwai.confucius.biz.po.systematism.HomeworkVote;
import com.iquanwai.confucius.biz.dao.survey.SurveyQuestionSubmitDao;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.survey.SurveyQuestionSubmit;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by nethunder on 2017/1/2.
 */
public class ProblemDaoTest extends TestBase {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ProblemDao problemDao;
    @Autowired
    private HomeworkVoteDao homeworkVoteDao;
    @Autowired
    private SurveyQuestionSubmitDao surveyQuestionSubmitDao;

    @Test
    public void timeTest(){
        Problem problem = problemDao.load(Problem.class,1);
    }

    @Test
    public void allVoteCunt(){
        List<HomeworkVote> list = homeworkVoteDao.allVoteList(1,1);
        System.out.println(list);
        System.out.println(list==null);
    }

    @Test
    public void testBatchInsert(){
        List<SurveyQuestionSubmit> surveyQuestionSubmits = Lists.newArrayList();
        SurveyQuestionSubmit surveyQuestionSubmit = new SurveyQuestionSubmit();
        surveyQuestionSubmit.setOpenId("12");
        surveyQuestionSubmit.setSurveySubmitId(1);
        surveyQuestionSubmit.setActivity(1);
        surveyQuestionSubmit.setContent("123");
        surveyQuestionSubmits.add(surveyQuestionSubmit);
        surveyQuestionSubmitDao.batchInsert(surveyQuestionSubmits);

    }
}
