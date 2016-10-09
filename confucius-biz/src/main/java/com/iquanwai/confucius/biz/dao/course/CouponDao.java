package com.iquanwai.confucius.biz.dao.course;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.Coupon;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by justin on 16/9/14.
 */
@Repository
public class CouponDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<Coupon> getCoupon(String openid){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Coupon>> h = new BeanListHandler(Coupon.class);

        try {
            List<Coupon> coupon = run.query("SELECT * FROM Coupon where Openid=? and Used=0 and ExpiredDate>=?",
                    h, openid, new Date());
            return coupon;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public void updateCoupon(List<Integer> couponIds, Integer status){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);

        String questionMark = produceQuestionMark(couponIds.size());
        try {
            asyncRun.update("UPDATE Coupon SET Status =? " +
                    "where Id in ("+questionMark+")", status, couponIds.toArray());

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public int insert(Coupon coupon){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);
        String insertSql = "INSERT INTO Coupon(Openid, Amount, Used, ExpiredDate) " +
                "VALUES(?, ?, ?, ?)";
        try {
            Future<Integer> result = asyncRun.update(insertSql,
                    coupon.getOpenid(), coupon.getAmount(), coupon.getUsed(), coupon.getExpiredDate());
            return result.get();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            // ignore
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
        }
        return -1;

    }
}
