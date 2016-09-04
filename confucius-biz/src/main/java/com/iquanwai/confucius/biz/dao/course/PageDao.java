package com.iquanwai.confucius.biz.dao.course;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.Page;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

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
}
