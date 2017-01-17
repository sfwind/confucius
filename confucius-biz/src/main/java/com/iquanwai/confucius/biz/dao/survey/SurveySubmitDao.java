package com.iquanwai.confucius.biz.dao.survey;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.survey.SurveySubmit;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/1/17.
 */
public class SurveySubmitDao extends DBUtil {
    public Logger logger = LoggerFactory.getLogger(this.getClass());


    public int insert(SurveySubmit surveySubmit){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into SurveySubmit(Activity, OpenId) " +
                "values(?, ?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    surveySubmit.getActivity(),surveySubmit.getOpenId());
            return insertRs.intValue();
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

}
