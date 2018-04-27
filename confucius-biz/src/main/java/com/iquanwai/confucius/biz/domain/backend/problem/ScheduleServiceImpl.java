package com.iquanwai.confucius.biz.domain.backend.problem;

import com.iquanwai.confucius.biz.dao.fragmentation.ProblemScheduleDao;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduleServiceImpl implements ScheduleService{

    @Autowired
    private ProblemScheduleDao problemScheduleDao;


    @Override
    public ProblemSchedule loadProblemSchedule(Integer problemId, Integer chapter, Integer section) {
        return problemScheduleDao.loadProblemSchedule(problemId,chapter,section);
    }
}
