package com.iquanwai.confucius.biz.dao.course;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.HomeworkSubmit;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
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

    public List<HomeworkSubmit> submittedHomework(Integer homeworkId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<HomeworkSubmit>> h = new BeanListHandler(HomeworkSubmit.class);

        try {
            List<HomeworkSubmit> submit = run.query("SELECT * FROM HomeworkSubmit where HomeworkId=? ", h, homeworkId);
            return submit;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    private boolean submitted(String openid, int classId, int homeworkId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<HomeworkSubmit> h = new BeanHandler(HomeworkSubmit.class);

        try {
            HomeworkSubmit submit = run.query("SELECT * FROM HomeworkSubmit where SubmitOpenid=? " +
                            "and ClassId=? and HomeworkId=?", h, openid, classId, homeworkId);
            if(submit==null){
                return false;
            }else{
                if(submit.getSubmitContent()==null){
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return false;
    }

    public HomeworkSubmit loadHomeworkSubmit(String openid, int classId, int homeworkId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<HomeworkSubmit> h = new BeanHandler(HomeworkSubmit.class);

        try {
            HomeworkSubmit submit = run.query("SELECT * FROM HomeworkSubmit where SubmitOpenid=? " +
                    "and ClassId=? and HomeworkId=?", h, openid, classId, homeworkId);
            return submit;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public int insert(String openid, int classId, int homeworkId, String submitUrl, String shortUrl) {
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);
        String insertSql = "INSERT INTO HomeworkSubmit(SubmitOpenid, ClassId, HomeworkId, SubmitUrl, ShortUrl) " +
                "VALUES(?, ?, ?, ?, ?)";
        try {
            Future<Integer> result = asyncRun.update(insertSql,
                    openid, classId, homeworkId, submitUrl, shortUrl);
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

    public void submit(int homeworkId, int classId, String openid, String submitContent){
        // 可以重复提交
//        if(submitted(openid, classId, homeworkId)){
//            return;
//        }
        QueryRunner run = new QueryRunner(getDataSource());

        try {
            run.update("UPDATE HomeworkSubmit SET SubmitContent =?, SubmitTime=now() " +
                    "where SubmitOpenid=? and ClassId=? and HomeworkId=?", submitContent, openid, classId, homeworkId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void remark(int homeworkId, int classId, String openid, String remark, Integer score){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);

        try {
            asyncRun.update("UPDATE HomeworkSubmit SET Remark =?, Score=? " +
                    "where SubmitOpenid=? and ClassId=? and HomeworkId=?", remark, score, openid, classId, homeworkId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
