package com.iquanwai.confucius.biz.dao.course;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.systematism.CourseOrder;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by justin on 16/9/10.
 */
@Repository
public class CourseOrderDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void insert(CourseOrder courseOrder) {
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);
        String insertSql = "INSERT INTO CourseOrder(OrderId, Openid, CourseId, ClassId, Entry, IsDel) " +
                "VALUES(?, ?, ?, ?, ?, ?)";
        try {
            asyncRun.update(insertSql,
                    courseOrder.getOrderId(), courseOrder.getOpenid(), courseOrder.getCourseId(),
                    courseOrder.getClassId(), courseOrder.getEntry(), courseOrder.getIsDel());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

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

    public List<CourseOrder> loadNotExpiredClassOrder(List<Integer> classId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<CourseOrder>> h = new BeanListHandler(CourseOrder.class);
        if(classId.isEmpty()){
            return Lists.newArrayList();
        }
        String questionMark = produceQuestionMark(classId.size());

        try {
            List<CourseOrder> order = run.query("SELECT * FROM CourseOrder where ClassId in ("+questionMark+") and IsDel=0", h, classId.toArray());
            return order;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public int underPaidCount(String openid, Integer classId){
        QueryRunner run = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<Long>();
        String sql = "SELECT count(*) FROM CourseOrder where Openid=? and ClassId=? and Entry=0 and IsDel=0";
        try {
            Long count = run.query(sql, h, openid, classId);
            return count.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return 0;
    }

    public void closeOrder(String orderId){
        QueryRunner run = new QueryRunner(getDataSource());
        String sql = "Update CourseOrder set IsDel=1 where OrderId=?";
        try {
            run.update(sql,orderId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void entry(String orderId){
        QueryRunner run = new QueryRunner(getDataSource());
        String sql = "Update CourseOrder set Entry=1 where OrderId=?";
        try {
            run.update(sql,orderId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
