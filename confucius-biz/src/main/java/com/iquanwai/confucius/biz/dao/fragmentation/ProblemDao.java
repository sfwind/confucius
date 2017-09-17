package com.iquanwai.confucius.biz.dao.fragmentation;

import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class ProblemDao extends PracticeDBUtil {

    Logger logger = LoggerFactory.getLogger(getClass());

    public Problem loadProblem(Integer problemId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM Problem WHERE Id = ?";
        ResultSetHandler<Problem> h = new BeanHandler<>(Problem.class);
        try {
            return runner.query(sql, h, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
