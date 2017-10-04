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
        String sql = "SELECT * FROM ProblemSchedule where ProblemId=?";
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
        String sql = "SELECT * FROM ProblemSchedule where KnowledgeId=?";
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
        String sql = "SELECT * FROM ProblemSchedule where ProblemId=? and Chapter=? and Section=?";
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

}
