package com.iquanwai.confucius.biz.dao.course;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.CourseOrder;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by justin on 16/9/10.
 */
public class CourseOrderDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(CourseOrder courseOrder) {
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);
        String insertSql = "INSERT INTO CourseOrder(OrderId, Openid, CourseId, ClassId" +
                ", Price, Discount, PrepayId, Status, PaidTime, CreateTime, ReturnMsg) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Future<Integer> result = asyncRun.update(insertSql,
                    courseOrder.getOrderId(), courseOrder.getOpenid(), courseOrder.getCourseId(),
                    courseOrder.getClassId(), courseOrder.getPrice(), courseOrder.getDiscount(),
                    courseOrder.getPrepayId(), courseOrder.getStatus(), courseOrder.getPaidTime(),
                    courseOrder.getCreateTime(), courseOrder.getReturnMsg());
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
}
