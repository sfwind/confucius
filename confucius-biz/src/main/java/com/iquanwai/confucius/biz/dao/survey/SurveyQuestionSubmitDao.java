package com.iquanwai.confucius.biz.dao.survey;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.survey.SurveyQuestionSubmit;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2017/1/17.
 */
@Repository
public class SurveyQuestionSubmitDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void batchInsert(List<SurveyQuestionSubmit> submitList){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into SurveyQuestionSubmit(SurveySubmitId, Activity, OpenId,QuestionLabel,Content) " +
                "VALUES (?, ?, ?, ?, ?)";
        try {
            Object[][] param = new Object[submitList.size()][];
            for (int i = 0; i < submitList.size(); i++) {
                SurveyQuestionSubmit submit = submitList.get(i);
                param[i] = new Object[5];
                param[i][0] = submit.getSurveySubmitId();
                param[i][1] = submit.getActivity();
                param[i][2] = submit.getOpenId();
                param[i][3] = submit.getQuestionLabel();
                param[i][4] = submit.getContent();
            }
            runner.batch(sql, param);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
