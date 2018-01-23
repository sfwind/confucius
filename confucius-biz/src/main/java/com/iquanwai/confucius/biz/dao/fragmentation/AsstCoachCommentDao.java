package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.ProfileCount;
import com.iquanwai.confucius.biz.po.fragmentation.AsstCoachComment;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2017/1/13.
 */
@Repository
public class AsstCoachCommentDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<AsstCoachComment> loadCommentedStudent(Integer problemId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<AsstCoachComment>> h = new BeanListHandler<>(AsstCoachComment.class);
        String sql = "SELECT * FROM AsstCoachComment where ProblemId=? And Del =0";
        try {
            return run.query(sql, h, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<ProfileCount> loadCommented(Integer problemId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ProfileCount>> h = new BeanListHandler<>(ProfileCount.class);
        String sql = "select ProfileId,SUM(Count) Count from AsstCoachComment where Del =0 and ProblemId = ? group by ProfileId";
        try {
            return run.query(sql, h, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public AsstCoachComment loadAsstCoachComment(Integer problemId, Integer profileId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<AsstCoachComment> h = new BeanHandler<>(AsstCoachComment.class);
        String sql = "SELECT * FROM AsstCoachComment where ProblemId=? and ProfileId=? And Del =0";
        try {
            return run.query(sql, h, problemId, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public void insert(AsstCoachComment asstCoachComment) {
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "insert into AsstCoachComment(ProfileId, Count, ProblemId) " +
                "VALUES (?,?,?)";
        try {
            run.insert(insertSql, new ScalarHandler<>(),
                    asstCoachComment.getProfileId(),
                    asstCoachComment.getCount(), asstCoachComment.getProblemId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateCount(AsstCoachComment asstCoachComment) {
        QueryRunner run = new QueryRunner(getDataSource());
        String updateSql = "Update AsstCoachComment set Count=? where Id=?";
        try {
            run.update(updateSql, asstCoachComment.getCount(), asstCoachComment.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}
