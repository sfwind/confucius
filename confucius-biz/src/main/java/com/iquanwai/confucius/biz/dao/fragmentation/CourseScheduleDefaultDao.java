package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.CourseScheduleDefault;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class CourseScheduleDefaultDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<CourseScheduleDefault> loadByCategory(Integer category) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM CourseScheduleDefault WHERE Category = ? AND Del = 0";
        ResultSetHandler<List<CourseScheduleDefault>> h = new BeanListHandler<>(CourseScheduleDefault.class);
        try {
            return runner.query(sql, h, category);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
