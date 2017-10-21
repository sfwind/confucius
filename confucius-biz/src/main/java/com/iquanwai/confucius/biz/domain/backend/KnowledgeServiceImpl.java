package com.iquanwai.confucius.biz.domain.backend;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.fragmentation.KnowledgeDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ProblemScheduleDao;
import com.iquanwai.confucius.biz.po.fragmentation.Knowledge;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 负责处理前端知识点导入功能
 */
@Service
public class KnowledgeServiceImpl implements KnowledgeService {
    @Autowired
    private ProblemScheduleDao problemScheduleDao;
    @Autowired
    private KnowledgeDao knowledgeDao;

    @Override
    public Knowledge loadKnowledge(Integer knowledgeId) {
        Knowledge knowledge = knowledgeDao.load(Knowledge.class, knowledgeId);
        ProblemSchedule problemSchedule = problemScheduleDao.loadProblemScheduleByKnowledge(knowledgeId);
        if (problemSchedule != null) {
            knowledge.setChapter(problemSchedule.getChapter());
            knowledge.setSection(problemSchedule.getSection());
        }
        return knowledge;
    }

    @Override
    public List<Knowledge> loadKnowledges(Integer problemId) {
        List<ProblemSchedule> problemSchedules = problemScheduleDao.loadProblemSchedule(problemId);
        List<Knowledge> knowledges = Lists.newArrayList();
        problemSchedules.forEach(problemSchedule -> {
            if (!Knowledge.isReview(problemSchedule.getKnowledgeId())) {
                knowledges.add(knowledgeDao.load(Knowledge.class, problemSchedule.getKnowledgeId()));
            }
        });
        return knowledges;
    }

    @Override
    public Integer updateKnowledge(Knowledge knowledge, Integer problemId) {
        if (knowledge.getId() != 0) {
            return knowledgeDao.updateKnowledge(knowledge);
        } else {
            ProblemSchedule problemSchedule = problemScheduleDao.loadProblemSchedule(problemId, knowledge.getChapter(), knowledge.getSection());
            if (problemSchedule != null) {
                return -1;
            }
            int knowledgeId = knowledgeDao.insertKnowledge(knowledge);
            knowledge.setId(knowledgeId);
            insertProblemSchedule(knowledge, problemId);

            return knowledgeId;
        }
    }

    private void insertProblemSchedule(Knowledge knowledge, Integer problemId) {
        ProblemSchedule schedule = new ProblemSchedule();
        List<ProblemSchedule> problemSchedules = problemScheduleDao.loadProblemSchedule(problemId);
        // 不精确计算series = 现有series+1
        int series = problemSchedules.size() + 1;
        schedule.setChapter(knowledge.getChapter());
        schedule.setSection(knowledge.getSection());
        schedule.setProblemId(problemId);
        schedule.setKnowledgeId(knowledge.getId());
        schedule.setSeries(series);
        problemScheduleDao.insert(schedule);
    }

}
