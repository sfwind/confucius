package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupChoice;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPractice;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 17/3/20.
 */
@Repository
public class WarmupChoiceDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<WarmupChoice> loadChoices(Integer practiceId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<WarmupChoice>> h = new BeanListHandler<>(WarmupChoice.class);
        String sql = "SELECT * FROM Choice where QuestionId = ? and Del=0";
        try {
            return run.query(sql, h, practiceId);
        } catch(SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public void updateChoice(WarmupChoice choice) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update Choice set Updated=1, Subject=?, IsRight=? where Id=?";
        try {
            runner.update(sql, choice.getSubject(), choice.getIsRight(), choice.getId());
        } catch(SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    // 批量插入数据
    public void batchInsert(List<WarmupChoice> choices) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into Choice (QuestionId, Sequence, Subject, IsRight) VALUES (?, ?, ?, ?)";
        try {
            Object[][] param = new Object[choices.size()][];
            for(int i = 0; i < choices.size(); i++) {
                WarmupChoice choice = choices.get(i);
                param[i] = new Object[4];
                param[i][0] = choice.getQuestionId();
                param[i][1] = choice.getSequence();
                param[i][2] = choice.getSubject();
                param[i][3] = choice.getIsRight();
            }
            runner.batch(sql, param);
        } catch(SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
    }


}
