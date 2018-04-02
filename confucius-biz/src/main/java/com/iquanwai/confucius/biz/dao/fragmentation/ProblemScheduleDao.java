package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSchedule;
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

@Repository
public class ProblemScheduleDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<ProblemSchedule> loadProblemSchedule(Integer problemId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ProblemSchedule>> h = new BeanListHandler<>(ProblemSchedule.class);
        String sql = "SELECT * FROM ProblemSchedule where ProblemId = ? AND Del = 0";
        try {
            return run.query(sql, h, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public ProblemSchedule loadProblemScheduleByKnowledge(Integer knowledgeId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ProblemSchedule> h = new BeanHandler<>(ProblemSchedule.class);
        String sql = "SELECT * FROM ProblemSchedule where KnowledgeId = ? AND Del = 0";
        try {
            return run.query(sql, h, knowledgeId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public ProblemSchedule loadProblemSchedule(Integer problemId, Integer chapter, Integer section) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ProblemSchedule> h = new BeanHandler<>(ProblemSchedule.class);
        String sql = "SELECT * FROM ProblemSchedule where ProblemId=? and Chapter=? and Section=? AND Del = 0";
        try {
            return run.query(sql, h, problemId, chapter, section);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public int insert(ProblemSchedule problemSchedule) {
        QueryRunner run = new QueryRunner(getDataSource());
        String sql = "INSERT INTO ProblemSchedule(ProblemId, KnowledgeId, Chapter, Section, Series) values(?,?,?,?,?)";
        try {
            Long result = run.insert(sql, new ScalarHandler<>(), problemSchedule.getProblemId(), problemSchedule.getKnowledgeId(),
                    problemSchedule.getChapter(), problemSchedule.getSection(), problemSchedule.getSeries());

            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 更新
     */
    public void update(ProblemSchedule problemSchedule) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ProblemSchedule set chapter = ?,series = ? where id = ?";
        try {
            runner.update(sql, problemSchedule.getChapter(), problemSchedule.getSeries(), problemSchedule.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return;
    }

    /**
     * 获得对应的复习ProblemSchedule
     */
    public List<ProblemSchedule> getReviewProblemSchedule(Integer problemId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<ProblemSchedule>> h = new BeanListHandler<>(ProblemSchedule.class);
        String sql = "select * from ProblemSchedule where problemId = ? and del = 0 and knowledgeId in (57,58,59)";
        try {
            List<ProblemSchedule> problemScheduleList = runner.query(sql, h, problemId);
            return problemScheduleList;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
