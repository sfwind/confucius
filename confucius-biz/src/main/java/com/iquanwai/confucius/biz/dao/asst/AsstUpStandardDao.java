package com.iquanwai.confucius.biz.dao.asst;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.asst.AsstUpStandard;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class AsstUpStandardDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 根据profileId查询用户的评判标准
     *
     * @param profileId
     * @return
     */
    public AsstUpStandard queryByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<AsstUpStandard> h = new BeanHandler<>(AsstUpStandard.class);
        String sql = "SELECT * FROM AsstUpStandard WHERE ProfileId = ? AND DEL = 0 ORDER BY ID DESC limit 1 ";

        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public Integer insert(AsstUpStandard asstUpStandard) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO AsstUpStandard(ProfileId,RoleId,CountDown,LearnedProblem," +
                "ApplicationRate,RequestReviewNumber,ValidReviewNumber,ValidReviewRate,HighQualityAnswer,HostNumber,HostScore," +
                "MainPointNumber,MainPointScore,OnlineAnswer,Swing,OnlineOrSwingNumber," +
                "OnlineScore,CampNumber,AsstNumber,CampScore,MonthlyWork,FosterNew," +
                "CompanyTrainNumber,CompanyTrainScore) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            Long result = runner.insert(sql, new ScalarHandler<>(), asstUpStandard.getProfileId(), asstUpStandard.getRoleId(),
                    asstUpStandard.getCountDown(), asstUpStandard.getLearnedProblem(), asstUpStandard.getApplicationRate(),
                    asstUpStandard.getRequestReviewNumber(), asstUpStandard.getValidReviewNumber(), asstUpStandard.getValidReviewRate(), asstUpStandard.getHighQualityAnswer(), asstUpStandard.getHostNumber(),
                    asstUpStandard.getHostScore(), asstUpStandard.getMainPointNumber(), asstUpStandard.getMainPointScore(),
                    asstUpStandard.getOnlineAnswer(), asstUpStandard.getSwing(), asstUpStandard.getOnlineOrSwingNumber(),
                    asstUpStandard.getOnlineScore(), asstUpStandard.getCampNumber(), asstUpStandard.getAsstNumber(),
                    asstUpStandard.getCampScore(), asstUpStandard.getMonthlyWork(), asstUpStandard.getFosterNew(),
                    asstUpStandard.getCompanyTrainNumber(), asstUpStandard.getCompanyTrainScore());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer update(AsstUpStandard asstUpStandard) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update AsstUpStandard SET CountDown = ?,LearnedProblem = ?,ApplicationRate = ?," +
                "RequestReviewNumber=?,ValidReviewNumber=?,ValidReviewRate=?,HighQualityAnswer=?,HostNumber =?," +
                "HostScore = ?,MainPointNumber =?,MainPointScore=?,OnlineAnswer=?,Swing = ?," +
                "OnlineOrSwingNumber = ?,OnlineScore=?,CampNumber=?,AsstNumber=?,CampScore=?," +
                "MonthlyWork = ?,FosterNew=?,CompanyTrainNumber=?,CompanyTrainScore = ? where id = ?";
        try {
            return runner.update(sql, asstUpStandard.getCountDown(), asstUpStandard.getLearnedProblem(), asstUpStandard.getApplicationRate(),
                    asstUpStandard.getRequestReviewNumber(), asstUpStandard.getValidReviewNumber(), asstUpStandard.getValidReviewRate(),
                    asstUpStandard.getHighQualityAnswer(), asstUpStandard.getHostNumber(), asstUpStandard.getHostScore(), asstUpStandard.getMainPointNumber(),
                    asstUpStandard.getMainPointScore(), asstUpStandard.getOnlineAnswer(), asstUpStandard.getSwing(),
                    asstUpStandard.getOnlineOrSwingNumber(), asstUpStandard.getOnlineScore(), asstUpStandard.getCampNumber(),
                    asstUpStandard.getAsstNumber(), asstUpStandard.getCampScore(), asstUpStandard.getMonthlyWork(),
                    asstUpStandard.getFosterNew(), asstUpStandard.getCompanyTrainNumber(), asstUpStandard.getCompanyTrainScore(),asstUpStandard.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
