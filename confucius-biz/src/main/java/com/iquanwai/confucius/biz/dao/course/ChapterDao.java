package com.iquanwai.confucius.biz.dao.course;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.Chapter;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 16/8/29.
 */
@Repository
public class ChapterDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<Chapter> loadChapters(int courseId, int week){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Chapter>> h = new BeanListHandler(Chapter.class);

        try {
            List<Chapter> chapterList = run.query("SELECT * FROM Chapter where CourseId=? and Week=?", h, courseId, week);
            return chapterList;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }
}
