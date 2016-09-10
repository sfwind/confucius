package com.iquanwai.confucius.biz.dao.course;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.ClassMember;
import com.iquanwai.confucius.biz.po.QuanwaiClass;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Created by justin on 16/8/29.
 */
@Repository
public class ClassDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<QuanwaiClass> openClass(String openid, int courseId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<QuanwaiClass>> h = new BeanListHandler(QuanwaiClass.class);

        try {
            List<QuanwaiClass> quanwaiClass = run.query("SELECT * FROM QuaiwaiClass where Openid=? and CourseId=? and Open = 1",
                    h, openid, courseId);

            if(CollectionUtils.isEmpty(quanwaiClass)){
                return null;
            }
            return quanwaiClass;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }
}
