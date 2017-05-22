package com.iquanwai.confucius.biz.dao.course;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.systematism.Course;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
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

    public List<Coupon> getCoupon(String openid) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Coupon>> h = new BeanListHandler<>(Coupon.class);

        try {
            List<Coupon> coupon = run.query("SELECT * FROM Coupon where Openid=? and Used in (0,2) and ExpiredDate>?",
                    h, openid, new Date());
            return coupon;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public List<Coupon> getUnusedCoupon() {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Coupon>> h = new BeanListHandler<>(Coupon.class);

        try {
            List<Coupon> coupon = run.query("SELECT * FROM Coupon where Used in (0,2) and >?",
                    h, new Date());
            return coupon;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    /**
     * 获取当前学员特定类型优惠券信息
     *
     * @return
     */
    public Coupon getCouponByCategory(String openId, String category) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<Coupon> h = new BeanHandler<>(Coupon.class);
        String sql = "select * from Coupon where openId = ? and category = ?";
        try {
            Coupon coupon = runner.query(sql, h, openId, category);
            return coupon;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public void updateCoupon(Integer couponId, Integer status, String orderId, Double cost) {
        QueryRunner run = new QueryRunner(getDataSource());
        try {
            run.update("UPDATE Coupon SET Used =?, OrderId=?, Cost=? " +
                    "where Id = ?", status, orderId, cost, couponId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateCouponByOrderId(Integer status, String orderId) {
        QueryRunner run = new QueryRunner(getDataSource());

        try {
            run.update("UPDATE Coupon SET Used =?, OrderId=null, Cost=null " +
                    "where OrderId = ?", status, orderId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public Integer updateExpiredDate(Coupon coupon) {
        QueryRunner runner = new QueryRunner(getDataSource());
        try {
            return runner.update("update Coupon set ExpiredDate = ? where OpenId = ? and Category = ?", ConfigUtils.getDiscountExpiredDate(), coupon.getOpenid(), coupon.getCategory());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return 0;
    }

    public void insert(Coupon coupon) {
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "INSERT INTO Coupon(Openid, Amount, Used, ExpiredDate) " +
                "VALUES(?, ?, ?, ?)";
        try {
            run.insert(insertSql, new ScalarHandler<>(),
                    coupon.getOpenid(), coupon.getAmount(), coupon.getUsed(), coupon.getExpiredDate());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void insertGroupCategory(Coupon coupon) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String insertSql = "insert into Coupon (OpenId, Amount, Used, ExpiredDate, Category, Description) " +
                "values (?, ?, ?, ?, ?, ?)";
        try {
            runner.insert(insertSql, new ScalarHandler<>(), coupon.getOpenid(), coupon.getAmount(), coupon.getUsed(),
                    coupon.getExpiredDate(), coupon.getCategory(), coupon.getDescription());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
