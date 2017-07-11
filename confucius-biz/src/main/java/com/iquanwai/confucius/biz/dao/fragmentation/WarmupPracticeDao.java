package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPractice;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 16/12/4.
 */
@Repository
public class WarmupPracticeDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<WarmupPractice> loadPractices(List<Integer> practiceIds){
        if(practiceIds.size()==0){
            return Lists.newArrayList();
        }
        QueryRunner run = new QueryRunner(getDataSource());
        String questionMark = produceQuestionMark(practiceIds.size());
        ResultSetHandler<List<WarmupPractice>> h = new BeanListHandler<>(WarmupPractice.class);
        String sql = "SELECT * FROM WarmupPractice where Id in ("+questionMark+")";
        try {
            return run.query(sql, h, practiceIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public List<WarmupPractice> loadPracticesByProblemId(Integer problemId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<WarmupPractice>> h = new BeanListHandler<>(WarmupPractice.class);
        String sql = "SELECT * FROM WarmupPractice where ProblemId=? and Del=0";
        try {
            return run.query(sql, h, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public void updateWarmupPractice(WarmupPractice warmupPractice) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update WarmupPractice set Updated=1, Question=?, Analysis=? where Id=?";
        try {
            runner.update(sql, warmupPractice.getQuestion(), warmupPractice.getAnalysis(), warmupPractice.getId());
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public WarmupPractice loadNextPractice(Integer problemId, Integer practiceId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<WarmupPractice> h = new BeanHandler<>(WarmupPractice.class);
        String sql = "SELECT * FROM WarmupPractice where ProblemId=? and Del=0 and Id>?";
        try {
            return run.query(sql, h, problemId, practiceId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    // 数据库插入巩固练习数据
    public Integer insertWarmupPractice(WarmupPractice practice) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into WarmupPractice (Question, Type, Analysis, Pic, Difficulty, " +
                "KnowledgeId, SceneId, ProblemId, Sequence, PracticeUid, Example, Updated) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(), practice.getQuestion(), practice.getType(), practice.getAnalysis(),
                    practice.getPic(), practice.getDifficulty(), practice.getKnowledgeId(), 1, practice.getProblemId(),
                    practice.getSequence(), practice.getPracticeUid(), practice.getExample(), practice.getUpdated());
            return result.intValue();
        } catch(SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return -1;
    }

}
