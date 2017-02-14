package com.iquanwai.confucius.biz.dao.wx;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
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
 * Created by justin on 17/1/19.
 */
@Repository
public class QuanwaiOrderDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void insert(QuanwaiOrder quanwaiOrder) {
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "INSERT INTO QuanwaiOrder(OrderId, Openid, Price, Discount, PrepayId, " +
                " Status, CreateTime, GoodsId, GoodsName, GoodsType) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            run.insert(insertSql, new ScalarHandler<>(),
                    quanwaiOrder.getOrderId(), quanwaiOrder.getOpenid(), quanwaiOrder.getPrice(),
                    quanwaiOrder.getDiscount(), quanwaiOrder.getPrepayId(), quanwaiOrder.getStatus(),
                    quanwaiOrder.getCreateTime(), quanwaiOrder.getGoodsId(), quanwaiOrder.getGoodsName(),
                    quanwaiOrder.getGoodsType());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public QuanwaiOrder loadOrder(String orderId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<QuanwaiOrder> h = new BeanHandler(QuanwaiOrder.class);

        try {
            QuanwaiOrder order = run.query("SELECT * FROM QuanwaiOrder where OrderId=? ", h, orderId);
            return order;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public void updatePrepayId(String prepayId, String orderId){
        QueryRunner run = new QueryRunner(getDataSource());

        try {
            run.update("UPDATE QuanwaiOrder SET PrepayId =? " +
                    "where OrderId=?", prepayId, orderId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void payError(String errMsg, String orderId){
        QueryRunner run = new QueryRunner(getDataSource());

        try {
            run.update("UPDATE QuanwaiOrder SET ReturnMsg =?, Status=4 " +
                    "where OrderId=?", errMsg, orderId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void paySuccess(Date paidTime, String transactionId, String orderId){
        QueryRunner run = new QueryRunner(getDataSource());

        try {
            run.update("UPDATE QuanwaiOrder SET Status=1, PaidTime =?, TransactionId=? " +
                    "where OrderId=?", paidTime, transactionId, orderId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<QuanwaiOrder> queryUnderCloseOrders(Date openTime) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<QuanwaiOrder>> h = new BeanListHandler(QuanwaiOrder.class);

        try {
            List<QuanwaiOrder> orderList = run.query("SELECT * FROM QuanwaiOrder where Status=0 and createTime<=? ",
                    h, openTime);
            return orderList;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public void closeOrder(String orderId){
        QueryRunner run = new QueryRunner(getDataSource());

        try {
            run.update("UPDATE QuanwaiOrder SET Status=2 " +
                    "where OrderId=?", orderId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
