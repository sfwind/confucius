package com.iquanwai.confucius.biz.dao.course;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.Coupon;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by justin on 16/9/14.
 */
@Repository
public class CouponDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<Coupon> loadCoupons(Integer profileId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Coupon>> h = new BeanListHandler<>(Coupon.class);

        try {
            return run.query("SELECT * FROM Coupon where ProfileId=? and Used = 0 and ExpiredDate>? and Del=0",
                    h, profileId, new Date());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public void updateCoupon(Integer couponId, String orderId, Double cost) {
        QueryRunner run = new QueryRunner(getDataSource());
        try {
            run.update("UPDATE Coupon SET OrderId=?, Cost=? " +
                    "where Id = ?", orderId, cost, couponId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateCouponByOrderId(Integer status, String orderId) {
        QueryRunner run = new QueryRunner(getDataSource());

        try {
            run.update("UPDATE Coupon SET Used =?, Cost=null " +
                    "where OrderId = ?", status, orderId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void insert(Coupon coupon) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String insertSql = "insert into Coupon (OpenId, ProfileId, Amount, Used, ExpiredDate, Category, Description) " +
                "values (?, ?, ?, ?, ?, ?, ?)";
        try {
            runner.insert(insertSql, new ScalarHandler<>(),
                    coupon.getOpenid(), coupon.getProfileId(),
                    coupon.getAmount(), coupon.getUsed(),
                    coupon.getExpiredDate(), coupon.getCategory(), coupon.getDescription());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
