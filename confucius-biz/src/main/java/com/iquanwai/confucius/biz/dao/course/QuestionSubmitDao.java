package com.iquanwai.confucius.biz.dao.course;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.QuestionSubmit;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by justin on 16/9/3.
 */
@Repository
public class QuestionSubmitDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public boolean submitted(String openid, int classId, int questionId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<QuestionSubmit> h = new BeanHandler(QuestionSubmit.class);

        try {
            QuestionSubmit submit = run.query("SELECT * FROM QuestionSubmit where SubmitOpenid=? " +
                    "and ClassId=? and QuestionId=?", h, openid, classId, questionId);
            if(submit==null){
                return false;
            }
            return true;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return false;
    }

    public int insert(QuestionSubmit questionSubmit) {
        if(submitted(questionSubmit.getSubmitOpenid(), questionSubmit.getClassId(), questionSubmit.getQuestionId())){
           return 0;
        }
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);
        String insertSql = "INSERT INTO QuestionSubmit(SubmitOpenid, ClassId, QuestionId, SubmitAnswer, SubmitTime, Score) " +
                "VALUES(?, ?, ?, ?, now(), ?)";
        try {
            Future<Integer> result = asyncRun.update(insertSql,
                    questionSubmit.getSubmitOpenid(), questionSubmit.getClassId(), questionSubmit.getQuestionId(),
                    questionSubmit.getSubmitAnswer(), questionSubmit.getScore());
            return result.get();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            // ignore
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
        }

        return -1;
    }
}
