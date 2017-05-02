package com.iquanwai.confucius.biz.dao.course;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.systematism.CourseIntroduction;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 16/9/29.
 */
@Repository
public class CourseIntroductionDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public CourseIntroduction getByCourseId(Integer courseId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<CourseIntroduction> h = new BeanHandler<>(CourseIntroduction.class);


        try {
            CourseIntroduction introduction = run.query("SELECT * FROM CourseIntroduction where CourseId=?",
                    h, courseId);

            return introduction;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }
}
