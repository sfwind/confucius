package com.iquanwai.confucius.biz.dao.apply;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.apply.InterviewRecord;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * 面试记录
 */
@Repository
public class InterviewRecordDao extends DBUtil {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public Integer insert(InterviewRecord interviewRecord) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO InterviewRecord(ProfileId,InterviewerId,ApprovalId,ApplyId,InterviewTime,Question,FocusChannel,FocusChannelName,TouchDuration,TouchDurationName,ApplyEvent,ApplyEventName," +
                "LearningWill,PotentialScore,ApplyAward,ApplyReason) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

        try {
            Long result = runner.insert(sql, new ScalarHandler<>(), interviewRecord.getProfileId(), interviewRecord.getInterviewerId(),interviewRecord.getApprovalId(),interviewRecord.getApplyId(), interviewRecord.getInterviewTime(), interviewRecord.getQuestion(),
                    interviewRecord.getFocusChannel(),interviewRecord.getFocusChannelName(), interviewRecord.getTouchDuration(),
                    interviewRecord.getTouchDurationName(),interviewRecord.getApplyEvent(),interviewRecord.getApplyEventName(),
                    interviewRecord.getLearningWill(), interviewRecord.getPotentialScore(),
                    interviewRecord.getApplyAward(), interviewRecord.getApplyReason());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer updateByAssist(InterviewRecord interviewRecord) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = " UPDATE InterviewRecord SET InterviewTime =?,Question =?,FocusChannel = ?,FocusChannelName = ? ,TouchDuration=?,TouchDurationName=?,ApplyEvent=?,ApplyEventName = ?,LearningWill = ?,  " +
                "  PotentialScore = ?,ApplyAward = ?,ApplyReason = ? WHERE ID = ? ";
        try {
           return runner.update(sql,interviewRecord.getInterviewTime(),interviewRecord.getQuestion(),interviewRecord.getFocusChannel(),
                   interviewRecord.getFocusChannelName(),interviewRecord.getTouchDuration(),interviewRecord.getTouchDurationName(),
                   interviewRecord.getApplyEvent(),interviewRecord.getApplyEventName(),
                    interviewRecord.getLearningWill(),interviewRecord.getPotentialScore(),interviewRecord.getApplyAward(),interviewRecord.getApplyReason(),interviewRecord.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return  -1;
    }

    public Integer updateByAdmin(InterviewRecord interviewRecord){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = " UPDATE InterviewRecord SET ApprovalId = ?, InterviewTime =?,Question =?,FocusChannel = ?,FocusChannelName = ? ,TouchDuration=?,TouchDurationName=?,ApplyEvent=?,ApplyEventName = ?,LearningWill = ?,  " +
                "  PotentialScore = ?,ApplyAward = ?,ApplyReason = ? WHERE ID = ? ";
        try {
            return runner.update(sql,interviewRecord.getApprovalId(),interviewRecord.getInterviewTime(),interviewRecord.getQuestion(),interviewRecord.getFocusChannel(),
                    interviewRecord.getFocusChannelName(),interviewRecord.getTouchDuration(),interviewRecord.getTouchDurationName(),
                    interviewRecord.getApplyEvent(),interviewRecord.getApplyEventName(),
                    interviewRecord.getLearningWill(),interviewRecord.getPotentialScore(),interviewRecord.getApplyAward(),interviewRecord.getApplyReason(),interviewRecord.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return  -1;
    }

    /**
     * 根据申请id获得面试评论
     * @param applyId
     * @return
     */
    public InterviewRecord queryByApplyId(Integer applyId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = " SELECT * FROM InterviewRecord WHERE APPLYID = ? AND DEL = 0 ";
        ResultSetHandler<InterviewRecord> h = new BeanHandler<>(InterviewRecord.class);
        try {
          return   runner.query(sql,h,applyId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return null;
    }
}
