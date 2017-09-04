package com.iquanwai.confucius.biz.dao.common.survey;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.common.survey.SurveyHref;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/9/4.
 */
@Repository
public class SurveyHrefDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public SurveyHref loadSurveyHref(Integer activity) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from SurveyHref where Activity = ? and Del = 0";
        try {
            return runner.query(sql, new BeanHandler<SurveyHref>(SurveyHref.class), activity);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public Integer insertSurveyHref(SurveyHref surveyHref) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Insert into SurveyHref (Activity,Name,RealHref) VALUES(?,?,?)";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(), surveyHref.getActivity(), surveyHref.getName(), surveyHref.getRealHref()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer updateSurveyHref(SurveyHref surveyHref) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update SurveyHref set Name = ?,RealHref = ?,Activity = ?,Del = ? where Id = ?";
        try {
            return runner.update(sql, surveyHref.getName(), surveyHref.getRealHref(), surveyHref.getActivity(), surveyHref.getDel(), surveyHref.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
