package com.iquanwai.confucius.biz.dao.course;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.ClassMember;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by justin on 16/8/29.
 */
@Repository
public class ClassMemberDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public ClassMember activeCourse(String openid, Integer courseId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ClassMember> h = new BeanHandler(ClassMember.class);

        try {
            ClassMember classMember = run.query("SELECT * FROM ClassMember where Openid=? and CourseId=? and Graduate = 0", h, openid, courseId);
            return classMember;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    @Deprecated
    public ClassMember activeCourse(String openid){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ClassMember> h = new BeanHandler(ClassMember.class);

        try {
            ClassMember classMember = run.query("SELECT * FROM ClassMember where Openid=? and Graduate = 0", h, openid);
            return classMember;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }


    public void progress(String openid, Integer classId, String progress){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);

        try {
            asyncRun.update("UPDATE ClassMember SET Progress =? " +
                    "where Openid=? and ClassId=?", progress, openid, classId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void complete(String openid, Integer classId, String complete){
        QueryRunner run = new QueryRunner(getDataSource());

        try {
            run.update("UPDATE ClassMember SET Complete =? " +
                    "where Openid=? and ClassId=?", complete, openid, classId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public ClassMember getClassMember(Integer classId, String openid){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ClassMember> h = new BeanHandler(ClassMember.class);

        try {
            ClassMember classMember = run.query("SELECT * FROM ClassMember where Openid=? and ClassId=? ",
                    h, openid, classId);
            return classMember;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public List<ClassMember> getClassMember(Integer classId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ClassMember>> h = new BeanListHandler(ClassMember.class);

        try {
            List<ClassMember> classMember = run.query("SELECT * FROM ClassMember where ClassId=? ",
                    h, classId);
            return classMember;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }
    public int entry(ClassMember classMember) {
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);
        String insertSql = "INSERT INTO ClassMember(ClassId, CourseId, Openid, MemberId, Graduate) " +
                "VALUES(?, ?, ?, ?, 0)";
        try {
            Future<Integer> result = asyncRun.update(insertSql,
                    classMember.getClassId(), classMember.getCourseId(), classMember.getOpenId(), classMember.getMemberId());
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

    public Integer classMemberNumber(Integer classId){
        QueryRunner run = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<Long>();

        try {
            Long number = run.query("SELECT count(*) FROM ClassMember where ClassId=?", h, classId);
            return number.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }

    public List<ClassMember> getPassMember(Integer classId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ClassMember>> h = new BeanListHandler(ClassMember.class);

        try {
            List<ClassMember> classMember = run.query("SELECT * FROM ClassMember where ClassId=? and Pass = 1 and Graduate = 0",
                    h, classId);
            return classMember;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public void graduate(Integer classId){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);

        try {
            asyncRun.update("UPDATE ClassMember SET Graduate =1 " +
                    "where ClassId=? and Graduate=0", classId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateCertificateNo(Integer classId, String openid, String certificateNo){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);

        try {
            asyncRun.update("UPDATE ClassMember SET CertificateNo=? " +
                    "where ClassId=? and openid=?", certificateNo, classId, openid);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
