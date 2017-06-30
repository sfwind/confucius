package com.iquanwai.confucius.biz.dao.course;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.systematism.HomeworkSubmit;
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

/**
 * Created by justin on 16/9/3.
 */
@Repository
public class HomeworkSubmitDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public HomeworkSubmit loadByUrl(String url) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<HomeworkSubmit> h = new BeanHandler<>(HomeworkSubmit.class);

        try {
            HomeworkSubmit submit = run.query("SELECT * FROM HomeworkSubmit where submitUrl=? ", h, url);
            return submit;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public List<HomeworkSubmit> submittedHomework(Integer homeworkId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<HomeworkSubmit>> h = new BeanListHandler<>(HomeworkSubmit.class);

        try {
            List<HomeworkSubmit> submit = run.query("SELECT * FROM HomeworkSubmit where HomeworkId=? ", h, homeworkId);
            return submit;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public HomeworkSubmit loadHomeworkSubmit(Integer profileId, int classId, int homeworkId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<HomeworkSubmit> h = new BeanHandler<>(HomeworkSubmit.class);

        try {
            HomeworkSubmit submit = run.query("SELECT * FROM HomeworkSubmit where SubmitProfileId=? " +
                    "and ClassId=? and HomeworkId=?", h, profileId, classId, homeworkId);
            return submit;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public int insert(HomeworkSubmit homeworkSubmit) {
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "INSERT INTO HomeworkSubmit(SubmitOpenid, SubmitProfileId, ClassId, HomeworkId, SubmitUrl, ShortUrl) " +
                "VALUES(?, ?, ?, ?, ?, ?)";
        try {
            run.insert(insertSql, new ScalarHandler<>(),
                    homeworkSubmit.getSubmitOpenid(), homeworkSubmit.getSubmitProfileId(),
                    homeworkSubmit.getClassId(), homeworkSubmit.getHomeworkId(),
                    homeworkSubmit.getSubmitUrl(), homeworkSubmit.getShortUrl());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }

    public void submit(int homeworkId, int classId, Integer profileId, String submitContent) {
        // 可以重复提交
//        if(submitted(openid, classId, homeworkId)){
//            return;
//        }
        QueryRunner run = new QueryRunner(getDataSource());

        try {
            run.update("UPDATE HomeworkSubmit SET SubmitContent =?, SubmitTime=now() " +
                            "where SubmitProfileId=? and ClassId=? and HomeworkId=?",
                    submitContent, profileId, classId, homeworkId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void remark(int homeworkId, int classId, Integer profileId, String remark, Integer score) {
        QueryRunner run = new QueryRunner(getDataSource());

        try {
            run.update("UPDATE HomeworkSubmit SET Remark =?, Score=? " +
                            "where SubmitProfileId=? and ClassId=? and HomeworkId=?",
                    remark, score, profileId, classId, homeworkId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
