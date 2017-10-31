package com.iquanwai.confucius.biz.dao;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.dao.fragmentation.ApplicationSubmitDao;
import com.iquanwai.confucius.biz.dao.fragmentation.FragmentAnalysisDataDao;
import com.iquanwai.confucius.biz.dao.fragmentation.HomeworkVoteDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ProblemDao;
import com.iquanwai.confucius.biz.po.fragmentation.FragmentDailyData;
import com.iquanwai.confucius.biz.po.systematism.HomeworkVote;
import com.iquanwai.confucius.biz.dao.common.survey.SurveyQuestionSubmitDao;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.common.survey.SurveyQuestionSubmit;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

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
    @Autowired
    private FragmentAnalysisDataDao fragmentAnalysisDataDao;
    @Autowired
    private ApplicationSubmitDao applicationSubmitDao;

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

    @Test
    public void testAnalysisDao(){
        FragmentDailyData dailyData = fragmentAnalysisDataDao.getDailyData();
        System.out.println(dailyData);
        fragmentAnalysisDataDao.insertDailyData(dailyData);
    }

    @Test
    public void testLoad(){
        Map<Integer, Integer> integerIntegerMap = applicationSubmitDao.loadUserSubmitCount();
        System.out.println(integerIntegerMap);
    }
}
