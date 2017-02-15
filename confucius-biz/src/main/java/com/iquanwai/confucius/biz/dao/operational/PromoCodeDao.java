package com.iquanwai.confucius.biz.dao.operational;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.PromoCode;
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
 * Created by justin on 17/2/14.
 */
@Repository
public class PromoCodeDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public PromoCode queryPromoCode(String code, String activityCode) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<PromoCode> h = new BeanHandler(PromoCode.class);
        String sql = "select * from PromoCode where Code=? and ActivityCode=?";
        try {
            return run.query(sql, h, code, activityCode);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public PromoCode queryPromoCodeByOwner(String owner, String activityCode) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<PromoCode> h = new BeanHandler(PromoCode.class);
        String sql = "select * from PromoCode where Owner=? and ActivityCode=?";
        try {
            return run.query(sql, h, owner, activityCode);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public List<PromoCode> queryPromoCodeByActivityCode(String activityCode) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<PromoCode>> h = new BeanListHandler(PromoCode.class);
        String sql = "select * from PromoCode where ActivityCode=?";
        try {
            return run.query(sql, h, activityCode);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public void incrementPromoCodeUsage(String code, String activityCode) {
        QueryRunner run = new QueryRunner(getDataSource());
        String sql = "update PromoCode set UseCount=UseCount+1 where Code=? and ActivityCode=?";
        try {
            run.update(sql, code, activityCode);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
