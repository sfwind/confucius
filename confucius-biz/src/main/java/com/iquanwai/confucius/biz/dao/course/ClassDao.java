package com.iquanwai.confucius.biz.dao.course;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.QuanwaiClass;
import com.iquanwai.confucius.biz.util.DateUtils;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by justin on 16/8/29.
 */
@Repository
public class ClassDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<QuanwaiClass> openClass(int courseId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<QuanwaiClass>> h = new BeanListHandler(QuanwaiClass.class);

        try {
            List<QuanwaiClass> quanwaiClass = run.query("SELECT * FROM QuanwaiClass where CourseId=? and Open = 1",
                    h, courseId);
            return quanwaiClass;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public List<QuanwaiClass> openClass(){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<QuanwaiClass>> h = new BeanListHandler(QuanwaiClass.class);

        try {
            List<QuanwaiClass> quanwaiClass = run.query("SELECT * FROM QuanwaiClass where Open = 1",
                    h);
            return quanwaiClass;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public boolean isOver(Integer classId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<QuanwaiClass> h = new BeanHandler(QuanwaiClass.class);

        try {
            QuanwaiClass quanwaiClass = run.query("SELECT * FROM QuanwaiClass where Id = ?",
                    h, classId);
            if(quanwaiClass==null){
                return true;
            }
            //大于结束日期8天就算终结
            Date completeDate = DateUtils.parseStringToDate(quanwaiClass.getCloseTime());
            Date closeDate = DateUtils.afterDays(completeDate, 8);
            return closeDate.before(new Date());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return true;
    }

    public List<QuanwaiClass> loadClassByOpenDate(Date date){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<QuanwaiClass>> h = new BeanListHandler(QuanwaiClass.class);

        String dateStr = DateUtils.parseDateToString(date);
        try {
            List<QuanwaiClass> quanwaiClass = run.query("SELECT * FROM QuanwaiClass where OpenTime=? ",
                    h, dateStr);
            return quanwaiClass;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public List<QuanwaiClass> loadRunningClass(){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<QuanwaiClass>> h = new BeanListHandler(QuanwaiClass.class);

        String now = DateUtils.parseDateToString(new Date());
        try {
            List<QuanwaiClass> quanwaiClass = run.query("SELECT * FROM QuanwaiClass where OpenTime<=? and CloseTime>=?",
                    h, now, now);
            return quanwaiClass;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public void progress(int classId, int progress){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);

        try {
            asyncRun.update("UPDATE QuanwaiClass SET Progress =?, Open=0 " +
                    "where Id=?", progress, classId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void closeEntry(int classId){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);

        try {
            asyncRun.update("UPDATE QuanwaiClass SET Open=0 " +
                    "where Id=?", classId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
