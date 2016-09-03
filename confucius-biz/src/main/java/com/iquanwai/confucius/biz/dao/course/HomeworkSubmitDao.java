package com.iquanwai.confucius.biz.dao.course;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.dao.po.HomeworkSubmit;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by justin on 16/9/3.
 */
@Repository
public class HomeworkSubmitDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public HomeworkSubmit loadByUrl(String url){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<HomeworkSubmit> h = new BeanHandler(HomeworkSubmit.class);

        try {
            HomeworkSubmit submit = run.query("SELECT * FROM HomeworkSubmit where submitUrl=? ", h, url);
            return submit;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public boolean submitted(String openid, int classId, int homeworkId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<HomeworkSubmit> h = new BeanHandler(HomeworkSubmit.class);

        try {
            HomeworkSubmit submit = run.query("SELECT * FROM HomeworkSubmit where SubmitOpenid=? " +
                            "and ClassId=? and HomeworkId=?", h, openid, classId, homeworkId);
            if(submit==null){
                return false;
            }
            return true;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return false;
    }

    public int insert(String openid, int classId, int homeworkId, String submitUrl) {
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);
        String insertSql = "INSERT INTO HomeworkSubmit(SubmitOpenid, ClassId, HomeworkId, SubmitUrl) " +
                "VALUES(?, ?, ?, ?)";
        try {
            Future<Integer> result = asyncRun.update(insertSql,
                    openid, classId, homeworkId, submitUrl);
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

    public void submit(int homeworkId, String openid, String submitContent){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);

        try {
            asyncRun.update("UPDATE HomeworkSubmit SET SubmitContent =?, SubmitTime=now() " +
                    "where SubmitOpenid=? and HomeworkId=?", submitContent, openid, homeworkId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void remark(int homeworkId, String openid, String remark, Integer score){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);

        try {
            asyncRun.update("UPDATE HomeworkSubmit SET Remark =?, Score=? " +
                    "where SubmitOpenid=? and HomeworkId=?", remark, score, openid, homeworkId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
