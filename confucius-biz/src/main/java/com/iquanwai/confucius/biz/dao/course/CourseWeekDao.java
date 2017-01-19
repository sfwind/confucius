package com.iquanwai.confucius.biz.dao.course;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.systematism.CourseWeek;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 16/9/22.
 */
@Repository
public class CourseWeekDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public CourseWeek getCourseWeek(Integer courseId, int week){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<CourseWeek> h = new BeanHandler(CourseWeek.class);

        try {
            CourseWeek courseWeek = run.query("SELECT * FROM CourseWeek where CourseId=? and Sequence=?",
                    h, courseId, week);
            return courseWeek;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }
}
