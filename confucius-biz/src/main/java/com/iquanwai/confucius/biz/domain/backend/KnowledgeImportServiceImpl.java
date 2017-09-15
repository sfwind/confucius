package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.dao.fragmentation.KnowledgeDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ProblemDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ProblemScheduleDao;
import com.iquanwai.confucius.biz.po.fragmentation.Knowledge;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSchedule;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 负责处理前端知识点导入功能
 */
@Service
public class KnowledgeImportServiceImpl implements KnowledgeImportService {

    @Autowired
    private ProblemDao problemDao;
    @Autowired
    private ProblemScheduleDao problemScheduleDao;
    @Autowired
    private KnowledgeDao knowledgeDao;

    Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Problem loadEditableProblem() {
        return problemDao.loadProblem(ConfigUtils.getEditableProblem());
    }

    @Override
    public List<ProblemSchedule> loadEditableProblemSchedules() {
        return problemScheduleDao.loadProblemSchedule(ConfigUtils.getEditableProblem());
    }

    @Override
    public Knowledge loadKnowledge(Integer knowledgeId) {
        return knowledgeDao.load(Knowledge.class, knowledgeId);
    }

    @Override
    public int addNewChapter(Integer chapter) {
        int knowledgeId = knowledgeDao.insertKnowledge(new Knowledge());
        if (knowledgeId < 0) return knowledgeId;

        ProblemSchedule schedule = new ProblemSchedule();
        schedule.setChapter(chapter);
        schedule.setSection(1);
        schedule.setProblemId(ConfigUtils.getEditableProblem());
        schedule.setKnowledgeId(knowledgeId);
        int result = problemScheduleDao.insertProblemSchedule(schedule);

        reSortSeries(ConfigUtils.getEditableProblem());
        return result;
    }

    @Override
    public int addNewSection(Integer chapter, Integer section) {
        int knowledgeId = knowledgeDao.insertKnowledge(new Knowledge());
        if (knowledgeId < 0) return knowledgeId;

        ProblemSchedule schedule = new ProblemSchedule();
        schedule.setChapter(chapter);
        schedule.setSection(section);
        schedule.setProblemId(ConfigUtils.getEditableProblem());
        schedule.setKnowledgeId(knowledgeId);
        int result = problemScheduleDao.insertProblemSchedule(schedule);

        reSortSeries(ConfigUtils.getEditableProblem());
        return result;
    }

    @Override
    public Integer updateKnowledge(Knowledge knowledge) {
        return knowledgeDao.updateKnowledge(knowledge.getId(), knowledge.getKnowledge(), knowledge.getStep(),
                knowledge.getAnalysis(), knowledge.getMeans(), knowledge.getKeynote());
    }

    /**
     * 按照顺序，依次更新 problemSchedule 表的 series 值
     */
    private void reSortSeries(Integer problemId) {
        List<ProblemSchedule> schedules = problemScheduleDao.loadProblemSchedule(problemId);
        if (schedules.size() == 0) return;

        schedules = schedules.stream().sorted(Comparator.comparing(ProblemSchedule::getChapter)).collect(Collectors.toList());
        schedules = schedules.stream().sorted(Comparator.comparing(ProblemSchedule::getSection)).collect(Collectors.toList());

        for (int i = 0; i < schedules.size(); i++) {
            problemScheduleDao.updateSeries(schedules.get(i).getId(), i + 1);
        }
    }

}
