package com.iquanwai.confucius.biz.dao.course;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.Page;
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
public class PageDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public Page loadPage(int chapterId, int pageSequence){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<Page> h = new BeanHandler(Page.class);

        try {
            Page page = run.query("SELECT * FROM Page where ChapterId=? and Sequence=?", h,
                    chapterId, pageSequence);
            return page;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public List<Page> loadPages(List<Integer> chapterIdList){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Page>> h = new BeanListHandler(Page.class);

        String questionMark = produceQuestionMark(chapterIdList.size());

        try {
            List<Page> page = run.query("SELECT * FROM Page where ChapterId in ("+questionMark+")", h,
                    chapterIdList.toArray());
            return page;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public Integer chapterPageNumber(int chapterId){
        QueryRunner run = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<Long>();

        try {
            Long number = run.query("SELECT count(*) FROM Page where ChapterId=?", h,
                    chapterId);
            return number.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }
}
