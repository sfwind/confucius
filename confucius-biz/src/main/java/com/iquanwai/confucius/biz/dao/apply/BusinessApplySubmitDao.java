package com.iquanwai.confucius.biz.dao.apply;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.apply.BusinessApplySubmit;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * @author nethunder
 * @version 2017-11-23
 * <p>
 * 商学院申请提交记录
 */
@Repository
public class BusinessApplySubmitDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void batchInsertApplySubmit(List<BusinessApplySubmit> submits) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO BusinessApplySubmit(ApplyId, QuestionId, ChoiceId, ChoiceText, UserValue) " +
                "VALUES (?,?,?,?,?) ";
        try {
            Object[][] param = new Object[submits.size()][];
            for (int i = 0; i < submits.size(); i++) {
                BusinessApplySubmit applySubmit = submits.get(i);
                param[i] = new Object[5];
                param[i][0] = applySubmit.getApplyId();
                param[i][1] = applySubmit.getQuestionId();
                param[i][2] = applySubmit.getChoiceId();
                param[i][3] = applySubmit.getChoiceText();
                param[i][4] = applySubmit.getUserValue();
            }
            runner.batch(sql, param);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<BusinessApplySubmit> loadByApplyId(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from BusinessApplySubmit where ApplyId = ? and del = 0";
        try {
            return runner.query(sql, new BeanListHandler<>(BusinessApplySubmit.class), id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
