package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.PracticePlan;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 16/12/4.
 */
@Repository
public class PracticePlanDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<PracticePlan> loadPracticePlan(Integer planId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<PracticePlan>> h = new BeanListHandler<>(PracticePlan.class);
        String sql = "SELECT * FROM PracticePlan where PlanId=? Order by Series";
        try {
            List<PracticePlan> practicePlans = run.query(sql, h,
                    planId);
            return practicePlans;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public PracticePlan loadPracticePlan(Integer planId, Integer practiceId, Integer type){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<PracticePlan> h = new BeanHandler<>(PracticePlan.class);
        String sql = "SELECT * FROM PracticePlan where PlanId=? and PracticeId=? and Type=?";
        try {
            PracticePlan practicePlan = run.query(sql, h,
                    planId, practiceId, type);
            return practicePlan;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public void complete(Integer id){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update PracticePlan set Status=1 where Id=?";
        try {
            runner.update(sql, id);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
