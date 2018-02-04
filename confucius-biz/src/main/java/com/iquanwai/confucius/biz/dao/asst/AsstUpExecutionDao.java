package com.iquanwai.confucius.biz.dao.asst;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.asst.AsstUpExecution;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;

@Repository
public class AsstUpExecutionDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 查询用户的完成情况
     *
     * @param profileId
     * @return
     */
    public AsstUpExecution queryByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<AsstUpExecution> h = new BeanHandler<AsstUpExecution>(AsstUpExecution.class);
        String sql = " SELECT * FROM AsstUpExecution WHERE ProfileId = ? AND DEL = 0 order by id desc limit 1 ";
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public Integer insert(Integer standardId,Integer profileId, Integer roleId, Date startDate){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = " INSERT INTO AsstUpExecution(StandardId,ProfileId,RoleId,StartDate) VALUES (?,?,?,?) ";

        try {
            Long result = runner.insert(sql,new ScalarHandler<>(),standardId,profileId,roleId,startDate);
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return -1;
    }


    /**
     * 更新用户完成情况信息
     *
     * @param asstUpExecution
     * @return
     */
    public Integer update(AsstUpExecution asstUpExecution) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = " Update AsstUpExecution SET ReviewNumber = ?, " +
                "RequestReviewNumber = ? ,ValidReviewNumber = ?,HighQualityAnswer = ?,HostNumber = ?,HostScore = ? ,MainPointNumber = ?,MainPointScore=?," +
                "OnlineAnswer =?,Swing = ?,OnlineOrSwingNumber =?,OnlineScore = ?,CampNumber = ?,AsstNumber = ?,CampScore = ?," +
                "MonthlyWork = ?,LackTask = ?,FosterNew = ?,CompanyTrainNumber = ?,CompanyTrainScore = ?,UpGrade=? Where id = ?";
        try {
            return runner.update(sql, asstUpExecution.getReviewNumber(), asstUpExecution.getRequestReviewNumber(),
                    asstUpExecution.getValidReviewNumber(), asstUpExecution.getHighQualityAnswer(), asstUpExecution.getHostNumber(), asstUpExecution.getHostScore(),
                    asstUpExecution.getMainPointNumber(), asstUpExecution.getMainPointScore(), asstUpExecution.getOnlineAnswer(),
                    asstUpExecution.getSwing(), asstUpExecution.getOnlineOrSwingNumber(), asstUpExecution.getOnlineScore(),
                    asstUpExecution.getCampNumber(), asstUpExecution.getAsstNumber(), asstUpExecution.getCampScore(),
                    asstUpExecution.getMonthlyWork(), asstUpExecution.getLackTask(),
                    asstUpExecution.getFosterNew(), asstUpExecution.getCompanyTrainNumber(),
                    asstUpExecution.getCompanyTrainScore(),asstUpExecution.getUpGrade(), asstUpExecution.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}




