package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.util.DateUtils;
import org.apache.commons.dbutils.AsyncQueryRunner;
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
import java.util.concurrent.Executors;

/**
 * Created by justin on 16/12/4.
 */
@Repository
public class ImprovementPlanDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    // 查询该用户付过费的plan
    public List<ImprovementPlan> loadUserPlans(String openId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ImprovementPlan WHERE Openid = ?";
        ResultSetHandler<List<ImprovementPlan>> h = new BeanListHandler(ImprovementPlan.class);
        try {
            List<ImprovementPlan> improvementPlans = runner.query(sql, h, openId);
            return improvementPlans;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public ImprovementPlan loadRunningPlan(String openid){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ImprovementPlan WHERE Openid=? and CloseDate>=? and Status = 1";
        ResultSetHandler<ImprovementPlan> h = new BeanHandler(ImprovementPlan.class);
        try {
            ImprovementPlan improvementPlan =runner.query(sql, h, openid, DateUtils.parseDateTimeToString(new Date()));
            return improvementPlan;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }


    public void updatePoint(Integer planId, Integer point){
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "UPDATE ImprovementPlan SET Point =? where Id=?";
        try {
            asyncRun.update(sql, point, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateApplicationComplete(Integer planId){
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "UPDATE ImprovementPlan SET ApplicationComplete = ApplicationComplete+1 where Id=?";
        try {
            asyncRun.update(sql, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
