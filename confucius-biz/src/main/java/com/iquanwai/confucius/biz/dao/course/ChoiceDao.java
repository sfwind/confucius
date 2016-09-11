package com.iquanwai.confucius.biz.dao.course;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.Choice;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 16/9/3.
 */
@Repository
public class ChoiceDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<Choice> loadChoices(int questionId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Choice>> h = new BeanListHandler(Choice.class);

        try {
            List<Choice> choices = run.query("SELECT * FROM Choice where QuestionId=?", h,
                    questionId);
            return choices;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public List<Choice> loadRightChoices(int questionId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Choice>> h = new BeanListHandler(Choice.class);

        try {
            List<Choice> choices = run.query("SELECT * FROM Choice where QuestionId=? AND `Right`=1", h,
                    questionId);
            return choices;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }
}
