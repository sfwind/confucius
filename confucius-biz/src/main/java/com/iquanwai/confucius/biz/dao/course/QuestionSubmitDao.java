package com.iquanwai.confucius.biz.dao.course;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.systematism.QuestionSubmit;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 16/9/3.
 */
@Repository
public class QuestionSubmitDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public boolean submitted(String openid, int classId, int questionId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<QuestionSubmit> h = new BeanHandler<>(QuestionSubmit.class);

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
        // 一个用户在一个班级里对一个选择题只能提交一次
        if(submitted(questionSubmit.getSubmitOpenid(), questionSubmit.getClassId(), questionSubmit.getQuestionId())){
           return 0;
        }
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "INSERT INTO QuestionSubmit(SubmitOpenid, ClassId, QuestionId, SubmitAnswer, SubmitTime, Score, `IsRight`) " +
                "VALUES(?, ?, ?, ?, now(), ?, ?)";
        try {
            Long result = run.insert(insertSql, new ScalarHandler<Long>(),
                    questionSubmit.getSubmitOpenid(), questionSubmit.getClassId(), questionSubmit.getQuestionId(),
                    questionSubmit.getSubmitAnswer(), questionSubmit.getScore(), questionSubmit.getIsRight());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return 0;
    }
    
}
