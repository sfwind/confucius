package com.iquanwai.confucius.biz.domain.backend;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.fragmentation.KnowledgeDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ProblemDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ProblemScheduleDao;
import com.iquanwai.confucius.biz.po.fragmentation.Knowledge;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSchedule;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 负责处理前端知识点导入功能
 */
@Service
public class KnowledgeServiceImpl implements KnowledgeService {
    @Autowired
    private ProblemScheduleDao problemScheduleDao;
    @Autowired
    private KnowledgeDao knowledgeDao;
    @Autowired
    private ProblemDao problemDao;

    private final static int REVIEW_KNOWLEDGE_ID = 59;

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

            List<ProblemSchedule> reviewSchedules = problemScheduleDao.getReviewProblemSchedule(problemId);

            //当插入知识点的时候需要判断是否需要插入复习Schedule
            if (CollectionUtils.isEmpty(reviewSchedules)) {
                insertProblemScehdule(problemId);
                reviewSchedules = problemScheduleDao.getReviewProblemSchedule(problemId);
            }
            //判断是否重复
            if (problemSchedule != null) {
                //如果正好不是复习，则认为章节重复
                if (problemSchedule.getChapter().intValue() != reviewSchedules.get(0).getChapter().intValue()) {
                    return -1;
                }
            }

            int knowledgeId = knowledgeDao.insertKnowledge(knowledge);
            knowledge.setId(knowledgeId);
            //插入目标知识点对应的Schedule
            insertProblemSchedule(knowledge, problemId);

            //当新增知识点章节大于目前最大章节时，进行更新操作
            reviewSchedules.stream().forEach(reviewSchedule -> {
                if (knowledge.getChapter() >= reviewSchedule.getChapter()) {
                    updateProblemSchedule(reviewSchedule.getId(), knowledge.getChapter() + 1, reviewSchedule.getSeries(), problemId);
                } else {
                    updateProblemSchedule(reviewSchedule.getId(), reviewSchedule.getChapter(), reviewSchedule.getSeries(), problemId);
                }
            });
            return knowledgeId;
        }
    }

    @Override
    public List<Knowledge> queryAllKnowLedges() {
        return knowledgeDao.queryAllKnowledges();
    }

    /**
     * 插入目标ProblemSchedule
     *
     * @param knowledge
     * @param problemId
     */
    private void insertProblemSchedule(Knowledge knowledge, Integer problemId) {
        ProblemSchedule schedule = new ProblemSchedule();
        List<ProblemSchedule> problemSchedules = problemScheduleDao.loadProblemSchedule(problemId);
        List<ProblemSchedule> unReviewdProblemSchedules = problemSchedules.stream().filter(problemSchedule ->
                (problemSchedule.getKnowledgeId() != 57 && problemSchedule.getKnowledgeId() != 58 && problemSchedule.getKnowledgeId() != 59)
        ).collect(Collectors.toList());

        int series = unReviewdProblemSchedules.size();
        schedule.setChapter(knowledge.getChapter());
        schedule.setSection(knowledge.getSection());
        schedule.setProblemId(problemId);
        schedule.setKnowledgeId(knowledge.getId());
        schedule.setSeries(series + 1);
        problemScheduleDao.insert(schedule);
    }

    /**
     * 更新
     *
     * @param id
     * @param chapter
     */
    private void updateProblemSchedule(Integer id, Integer chapter, Integer series, Integer problemId) {
        ProblemSchedule schedule = new ProblemSchedule();

        schedule.setId(id);
        schedule.setChapter(chapter);
        schedule.setSeries(series + 1);

        problemScheduleDao.update(schedule);
    }

    @Override
    public int insertProblemScehdule(Integer problemId) {
        ProblemSchedule problemSchedule = new ProblemSchedule();
        problemSchedule.setProblemId(problemId);
        problemSchedule.setKnowledgeId(REVIEW_KNOWLEDGE_ID);
        problemSchedule.setChapter(1);
        problemSchedule.setSection(1);
        problemSchedule.setSeries(1);

        return problemScheduleDao.insert(problemSchedule);
    }

    @Override
    public List<ProblemSchedule> loadKnowledgesGroupByProblem() {
        List<Problem> problems = problemDao.loadAll(Problem.class);
        List<ProblemSchedule> problemSchedules = problemScheduleDao.loadAll(ProblemSchedule.class);
        // 取出所有知识点列表，并且将知识点列表转换成键值对
        List<Knowledge> knowledges = knowledgeDao.loadAll(Knowledge.class);
        Map<Integer, Knowledge> knowledgeMap = Maps.newHashMap();
        knowledges.forEach(knowledge -> knowledgeMap.put(knowledge.getId(), knowledge));
        // 过滤出未被删除的课程列表
        List<Problem> validProblems = problems.stream().filter(problem -> !problem.getDel()).collect(Collectors.toList());
        // 逐个遍历课程，并将该课程，与该课程对应的所有知识点合并成一个对象进行返回
        List<ProblemSchedule> problemAndKnowledges = validProblems.stream().map(problem -> {
            ProblemSchedule targetProblemSchedule = new ProblemSchedule();
            // 取出该课程的 id
            Integer problemId = problem.getId();
            targetProblemSchedule.setId(problemId);
            targetProblemSchedule.setProblemId(problemId);
            // 根据取出的课程 id，遍历 problemSchedules 列表，取出二者 problemId 相同对象，并返回该所有对象的相关 KnowledgeId 的集合
            List<Integer> relatedKnowledgeIds = problemSchedules.stream().filter(problemSchedule -> problemId.equals(problemSchedule.getProblemId()))
                    .map(ProblemSchedule::getKnowledgeId).collect(Collectors.toList());
            List<Knowledge> targetKnowledges = Lists.newArrayList();
            relatedKnowledgeIds.forEach(relatedKnowledgeId -> {
                Knowledge targetKnowledge = knowledgeMap.get(relatedKnowledgeId);
                if(targetKnowledge != null) {
                    targetKnowledges.add(targetKnowledge);
                }
            });
            targetProblemSchedule.setKnowledges(targetKnowledges);
            return targetProblemSchedule;
        }).collect(Collectors.toList());
        return problemAndKnowledges;
    }
}


