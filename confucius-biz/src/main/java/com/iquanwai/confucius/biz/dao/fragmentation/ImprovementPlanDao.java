package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.util.DateUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by justin on 16/12/4.
 */
@Repository
public class ImprovementPlanDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<ImprovementPlan> loadPlans(Integer profileId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql ="SELECT * FROM ImprovementPlan WHERE ProfileId = ?";
        ResultSetHandler<List<ImprovementPlan>> h = new BeanListHandler<>(ImprovementPlan.class);
        try {
            return runner.query(sql,h,profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return Lists.newArrayList();
    }

    public List<ImprovementPlan> loadAllPlans(Integer profileId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ImprovementPlan WHERE ProfileId=? and Del=0";
        ResultSetHandler<List<ImprovementPlan>> h = new BeanListHandler<>(ImprovementPlan.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }


    public void updatePoint(Integer planId, Integer point){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE ImprovementPlan SET Point =? where Id=?";
        try {
            runner.update(sql, point, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateApplicationComplete(Integer planId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE ImprovementPlan SET ApplicationComplete = ApplicationComplete+1 where Id=?";
        try {
            runner.update(sql, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void becomeRiseMember(ImprovementPlan plan){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE ImprovementPlan SET RiseMember = 1,CloseDate = ? where Id=?";
        try {
            runner.update(sql, plan.getCloseDate(),plan.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void becomeRiseEliteMember(ImprovementPlan plan){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE ImprovementPlan SET RiseMember = 1,CloseDate = ?,RequestCommentCount=1 where Id=?";
        try {
            runner.update(sql, plan.getCloseDate(),plan.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }


    public void updateRequestComment(Integer planId, Integer count){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE ImprovementPlan SET RequestCommentCount = ? where Id=?";
        try {
            runner.update(sql, count, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

    }
}
