package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSchedule;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
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

    public Integer insertProblemSchedule(ProblemSchedule problemSchedule) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO ProblemSchedule (ProblemId, Chapter, Section, Series, KnowledgeId)" +
                "VALUES (?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    problemSchedule.getProblemId(),
                    problemSchedule.getChapter(),
                    problemSchedule.getSection(),
                    problemSchedule.getSeries(),
                    problemSchedule.getKnowledgeId());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public void updateSeries(Integer problemScheduleId, Integer series) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE ProblemSchedule SET Series = ? WHERE Id = ?";
        try {
            runner.update(sql, series, problemScheduleId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}
