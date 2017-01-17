package com.iquanwai.confucius.biz.dao.survey;

import com.iquanwai.confucius.biz.dao.DBUtil;
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

}
