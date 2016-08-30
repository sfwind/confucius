package com.iquanwai.confucius.biz.dao;

import com.iquanwai.confucius.biz.dao.po.ClassMember;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 16/8/29.
 */
@Repository
public class ClassMemberDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public ClassMember activeCourse(String openid){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ClassMember> h = new BeanHandler<ClassMember>(ClassMember.class);

        try {
            ClassMember classMember = run.query("SELECT * FROM ClassMember where Openid=? and Graduate = 0", h, openid);
            return classMember;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }
}
