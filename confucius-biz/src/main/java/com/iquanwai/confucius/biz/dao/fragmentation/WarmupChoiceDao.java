package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupChoice;
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
        String sql = "SELECT * FROM Choice where QuestionId = ?";
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

    // 插入选择题数据
    public Integer insertChoice(WarmupChoice choice) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into Choice (QuestionId, Sequence, Subject, IsRight) VALUES (?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(), choice.getQuestionId(), choice.getSequence(),
                    choice.getSubject(), choice.getIsRight());
            return result.intValue();
        } catch(SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return -1;
    }

}
