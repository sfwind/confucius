package com.iquanwai.confucius.biz.dao.course;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.dao.po.ClassMember;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.concurrent.Executors;

/**
 * Created by justin on 16/8/29.
 */
@Repository
public class ClassMemberDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

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
}
