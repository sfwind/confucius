package com.iquanwai.confucius.biz.dao.fragmentation;

import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class CommentEvaluationDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void initCommentEvaluation(Integer submitId, Integer commentId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO CommentEvaluation (SubmitId, CommentId) VALUES (?, ?)";
        try {
            runner.insert(sql, new ScalarHandler<>(), submitId, commentId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}
