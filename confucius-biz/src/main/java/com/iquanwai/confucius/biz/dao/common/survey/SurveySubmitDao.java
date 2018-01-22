package com.iquanwai.confucius.biz.dao.common.survey;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.common.survey.SurveySubmit;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/1/17.
 */
@Repository
public class SurveySubmitDao extends DBUtil {
    public Logger logger = LoggerFactory.getLogger(this.getClass());


    public int insert(String openId, Integer activity) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into SurveySubmit(Activity, OpenId) " +
                "values(?, ?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    activity, openId);
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Boolean submit(SurveySubmit surveySubmit) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update SurveySubmit SET Sequence = ?,Status = 1,TimeTaken = ?,SubmitTime = ?,TotalValue= ? where Id = ?";
        try {
            runner.update(sql, surveySubmit.getSequence(), surveySubmit.getTimeTaken(), surveySubmit.getSubmitTime(), surveySubmit.getTotalValue(), surveySubmit.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

}
