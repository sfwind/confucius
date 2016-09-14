package com.iquanwai.confucius.biz.dao.course;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.CourseOrder;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by justin on 16/9/10.
 */
@Repository
public class CourseOrderDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(CourseOrder courseOrder) {
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);
        String insertSql = "INSERT INTO CourseOrder(OrderId, Openid, CourseId, CourseName, ClassId" +
                ", Price, Discount, PrepayId, Status, PaidTime, CreateTime, ReturnMsg, TransactionId) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Future<Integer> result = asyncRun.update(insertSql,
                    courseOrder.getOrderId(), courseOrder.getOpenid(), courseOrder.getCourseId(),
                    courseOrder.getCourseName(), courseOrder.getClassId(), courseOrder.getPrice(),
                    courseOrder.getDiscount(), courseOrder.getPrepayId(), courseOrder.getStatus(),
                    courseOrder.getPaidTime(), courseOrder.getCreateTime(), courseOrder.getReturnMsg(),
                    courseOrder.getTransactionId());
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

    public CourseOrder loadOrder(String orderId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<CourseOrder> h = new BeanHandler(CourseOrder.class);

        try {
            CourseOrder order = run.query("SELECT * FROM CourseOrder where OrderId=? ", h, orderId);
            return order;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public int paidCount(Integer classId){
        QueryRunner run = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<Long>();

        try {
            Long count = run.query("SELECT count(*) FROM CourseOrder where ClassId=? and Status=1",
                    h, classId);
            return count.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return 0;
    }

    public void updatePrepayId(String prepayId, String orderId){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);

        try {
            asyncRun.update("UPDATE CourseOrder SET PrepayId =? " +
                    "where OrderId=?", prepayId, orderId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void payError(String errMsg, String orderId){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);

        try {
            asyncRun.update("UPDATE CourseOrder SET ReturnMsg =?, Status=4 " +
                    "where OrderId=?", errMsg, orderId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void paySuccess(Date paidTime, String transactionId, String orderId){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);

        try {
            asyncRun.update("UPDATE CourseOrder SET PaidTime =?, TransactionId=? " +
                    "where OrderId=?", paidTime, transactionId, orderId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
