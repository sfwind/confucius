package com.iquanwai.confucius.biz.dao.course;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.CourseFreeList;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 16/9/10.
 */
@Repository
public class CourseFreeListDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public boolean isFree(String openid, Integer courseId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<CourseFreeList> h = new BeanHandler(CourseFreeList.class);

        try {
            CourseFreeList freeList = run.query("SELECT * FROM CourseFreeList where Openid=? and CourseId=?",
                    h, openid, courseId);
            if(freeList==null){
                return false;
            }
            return true;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return false;
    }
}
