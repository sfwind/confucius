package com.iquanwai.confucius.biz.dao.operational;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.PromoCode;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

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

    public void incrementPromoCodeUsage(String code, String activityCode) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<PromoCode> h = new BeanHandler(PromoCode.class);
        String sql = "update PromoCode set Usage=Usage+1 where Code=? and ActivityCode=?";
        try {
            run.update(sql, h, code, activityCode);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
