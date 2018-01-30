package com.iquanwai.confucius.biz.dao.asst;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.asst.AsstUpDefault;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class AsstUpDefaultDao  extends DBUtil{

    private Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 根据教练级别加载默认的助教升级标准
     * @param roleId
     * @return
     */
    public AsstUpDefault queryByRoleId(Integer roleId){
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<AsstUpDefault> h = new BeanHandler<AsstUpDefault>(AsstUpDefault.class);
        String sql = " SELECT * FROM AsstUpDefault WHERE RoleId = ? AND DEL = 0 ";

        try {
           return runner.query(sql,h,roleId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return null;
    }

    public Integer update(AsstUpDefault asstUpDefault){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = " Update AsstUpDefault set CountDown = ?,LearnedProblem = ?,ApplicationRate = ?,RequestReviewNumber = ?,ValidReviewNumber = ?," +
                "ValidReviewRate = ?, HighQualityAnswer = ?,HostNumber = ?,HostScore = ?,MainPointNumber = ?," +
                "MainPointScore = ?,OnlineAnswer = ?,Swing = ?,OnlineOrSwingNumber = ?,OnlineScore=?," +
                "CampNumber =?,AsstNumber =?,CampScore =?,MonthlyWork=?,FosterNew=?,CompanyTrainNumber=?," +
                "CompanyTrainScore = ?,NeedVerified where id = ?";
        try {
           return runner.update(sql,asstUpDefault.getCountDown(),asstUpDefault.getLearnedProblem(),asstUpDefault.getApplicationRate(),
                    asstUpDefault.getRequestReviewNumber(),asstUpDefault.getValidReviewNumber(),asstUpDefault.getValidReviewRate(),asstUpDefault.getHighQualityAnswer(),
                    asstUpDefault.getHostNumber(),asstUpDefault.getHostScore(),asstUpDefault.getMainPointNumber(),
                    asstUpDefault.getMainPointScore(),asstUpDefault.getOnlineAnswer(),asstUpDefault.getSwing(),
                    asstUpDefault.getOnlineOrSwingNumber(),asstUpDefault.getOnlineScore(),asstUpDefault.getCampNumber(),
                    asstUpDefault.getAsstNumber(),asstUpDefault.getCampScore(),asstUpDefault.getMonthlyWork(),
                    asstUpDefault.getFosterNew(),asstUpDefault.getCompanyTrainNumber(),asstUpDefault.getCompanyTrainScore(),
                    asstUpDefault.getNeedVerified(),
                    asstUpDefault.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return -1;
    }
}
