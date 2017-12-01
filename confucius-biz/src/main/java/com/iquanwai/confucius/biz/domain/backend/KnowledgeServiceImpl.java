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

    private final int review_knowledgeId = 59;

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
            //插入知识点操作
        } else {
            ProblemSchedule problemSchedule = problemScheduleDao.loadProblemSchedule(problemId, knowledge.getChapter(), knowledge.getSection());

            ProblemSchedule reviewSchedule = problemScheduleDao.getMaxProblemSchedule(problemId);

            //当插入知识点的时候需要判断是否需要插入复习Schedule
            if(reviewSchedule == null){
                insertProblemScehdule(problemId);
                reviewSchedule = problemScheduleDao.getMaxProblemSchedule(problemId);
            }
            //判断是否重复
            if (problemSchedule != null) {
                //如果正好为复习，则不认为章节重复
                if(problemSchedule.getChapter().intValue()==reviewSchedule.getChapter().intValue()&& problemSchedule.getSection().intValue()==reviewSchedule.getSection().intValue()){

                }
                else{
                    return -1;
                }
            }

            int knowledgeId = knowledgeDao.insertKnowledge(knowledge);
            knowledge.setId(knowledgeId);
            insertProblemSchedule(knowledge, problemId);

            //当新增知识点章节大于目前最大章节时，进行更新操作
            if (knowledge.getChapter() >= reviewSchedule.getChapter()) {
                updateProblemSchedule(reviewSchedule.getId(), knowledge.getChapter() + 1, problemId);
            }
            else{
                updateProblemSchedule(reviewSchedule.getId(),reviewSchedule.getChapter(),problemId);
            }
            return knowledgeId;
        }
    }

    @Override
    public List<Knowledge> queryAllKnowLedges() {
        return knowledgeDao.queryAllKnowledges();
    }

    private void insertProblemSchedule(Knowledge knowledge, Integer problemId) {
        ProblemSchedule schedule = new ProblemSchedule();
        List<ProblemSchedule> problemSchedules = problemScheduleDao.loadProblemSchedule(problemId);
        int series = problemSchedules.size();
        schedule.setChapter(knowledge.getChapter());
        schedule.setSection(knowledge.getSection());
        schedule.setProblemId(problemId);
        schedule.setKnowledgeId(knowledge.getId());
        schedule.setSeries(series);
        problemScheduleDao.insert(schedule);
    }

    /**
     * 更新
     *
     * @param id
     * @param chapter
     */
    private void updateProblemSchedule(Integer id, Integer chapter, Integer problemId) {
        ProblemSchedule schedule = new ProblemSchedule();
        List<ProblemSchedule> problemSchedules = problemScheduleDao.loadProblemSchedule(problemId);

        int series = problemSchedules.size();
        schedule.setId(id);
        schedule.setChapter(chapter);
        schedule.setSeries(series);

        problemScheduleDao.update(schedule);
    }

    @Override
    public int insertProblemScehdule(Integer problemId) {
        ProblemSchedule problemSchedule = new ProblemSchedule();
        problemSchedule.setProblemId(problemId);
        problemSchedule.setKnowledgeId(review_knowledgeId);
        problemSchedule.setChapter(1);
        problemSchedule.setSection(1);
        problemSchedule.setSeries(1);

        return problemScheduleDao.insert(problemSchedule);
    }
}


