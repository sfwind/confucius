package com.iquanwai.confucius.biz.domain.backend;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.fragmentation.ProblemCatalogDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ProblemDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ProblemScheduleDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ProblemSubCatalogDao;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemCatalog;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSchedule;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSubCatalog;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2016/12/28.
 */
@Service
public class ProblemServiceImpl implements ProblemService {
    @Autowired
    private ProblemDao problemDao;
    @Autowired
    private ProblemCatalogDao problemCatalogDao;
    @Autowired
    private ProblemSubCatalogDao problemSubCatalogDao;
    @Autowired
    private ProblemScheduleDao problemScheduleDao;

    //缓存问题
    private List<Problem> problems = Lists.newArrayList();

    private int review_knowledgeId = 59;

    @Override
    public List<Problem> loadProblems() {
        if (CollectionUtils.isEmpty(problems)) {
            List<Problem> problems = problemDao.loadAll(Problem.class);
            this.problems = problems.stream().filter(problem -> !problem.getDel()).collect(Collectors.toList());
        }
        return problems;
    }

    @Override
    public List<ProblemCatalog> loadAllCatalogs() {
        return problemCatalogDao.loadAll(ProblemCatalog.class);
    }

    @Override
    public List<ProblemSubCatalog> loadAllSubCatalogs() {
        return problemSubCatalogDao.loadAll(ProblemSubCatalog.class);
    }

    @Override
    public List<ProblemSchedule> loadProblemSchedules(Integer problemId) {
        return problemScheduleDao.loadProblemSchedule(problemId);
    }

    @Override
    public Problem getProblem(Integer problemId) {
        if (CollectionUtils.isEmpty(problems)) {
            problems = loadProblems();
        }

        for (Problem problem : problems) {
            if (problem.getId() == problemId) {
                return problem;
            }
        }

        return null;
    }

    @Override
    public int saveProblem(Problem problem) {
        //清除缓存
        problems.clear();
        if (problem.getId() != 0) {
            problemDao.updateProblem(problem);
            return problem.getId();
        } else {
            return problemDao.saveProblem(problem);
        }
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

    /**
     * 判断是否有复习章节
     *
     * @param problemId
     * @return
     */
    @Override
    public boolean isHasReviewProblemSchedule(Integer problemId) {
        List<ProblemSchedule> reviewProblemSchedule = problemScheduleDao.getReviewProblemSchedule(problemId);
        if (reviewProblemSchedule == null || reviewProblemSchedule.size() == 0) {
            return false;
        }
        return true;
    }
}
