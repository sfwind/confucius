package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationPractice;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2017/1/13.
 */
@Repository
public class ApplicationPracticeDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<ApplicationPractice> getPracticeByProblemId(Integer problemId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ApplicationPractice>> h = new BeanListHandler<>(ApplicationPractice.class);
        String sql = "SELECT * FROM ApplicationPractice where ProblemId=? and Del=0";
        try {
            return run.query(sql, h, problemId);
        } catch(SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<ApplicationPractice> getAllPracticeByProblemId(Integer problemId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ApplicationPractice>> h = new BeanListHandler<>(ApplicationPractice.class);
        String sql = "SELECT * FROM ApplicationPractice where ProblemId=?";
        try {
            return run.query(sql, h, problemId);
        } catch(SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer updateApplicationPracticeById(Integer id, String topic, String description) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationPractice set topic = ?, description = ?, updated = 1 where id = ?";
        try {
            return runner.update(sql, topic, description, id);
        } catch(SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }


}
