package com.iquanwai.confucius.biz.dao.course;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.CurrentChapterPage;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by justin on 16/9/3.
 */
@Repository
public class CurrentChapterPageDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public Integer currentPage(String openid, int chapterId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<CurrentChapterPage> h = new BeanHandler(CurrentChapterPage.class);

        try {
            CurrentChapterPage page = run.query("SELECT PageSequence FROM CurrentChapterPage where ChapterId=? and Openid=?",
                    h, chapterId, openid);
            if(page==null){
                return null;
            }
            return page.getPageSequence();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return 0;
    }

    public void updatePage(String openid, int chapterId, int pageSequence){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);

        Integer value = currentPage(openid, chapterId);
        if(value==null){
            insert(openid, chapterId, pageSequence, asyncRun);
        }else{
            String updateSql = "UPDATE CurrentChapterPage SET PageSequence=? WHERE Openid=? AND ChapterId=? ";
            try {
                asyncRun.update(updateSql,
                        pageSequence, openid, chapterId);
            } catch (SQLException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
    }

    public int insert(String openid, int chapterId, int pageSequence, AsyncQueryRunner asyncRun) {
        String insertSql = "INSERT INTO CurrentChapterPage(Openid, ChapterId, PageSequence) " +
                "VALUES(?, ?, ?)";
        try {
            Future<Integer> result = asyncRun.update(insertSql,
                    openid, chapterId, pageSequence);
            return result.get();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            // ignore
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
        }

        return -1;
    }
}
