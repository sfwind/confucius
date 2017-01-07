package com.iquanwai.confucius.biz.dao;

import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.dao.fragmentation.HomeworkVoteDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ProblemDao;
import com.iquanwai.confucius.biz.po.HomeworkVote;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by nethunder on 2017/1/2.
 */
public class ProblemDaoTest extends TestBase {
    @Autowired
    private ProblemDao problemDao;
    @Autowired
    private HomeworkVoteDao homeworkVoteDao;
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
}
