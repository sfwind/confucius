package com.iquanwai.confucius.biz.dao.course;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.systematism.CurrentChapterPage;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 16/9/3.
 */
@Repository
public class CurrentChapterPageDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public Integer currentPage(Integer profileId, int chapterId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<CurrentChapterPage> h = new BeanHandler<>(CurrentChapterPage.class);

        try {
            CurrentChapterPage page = run.query("SELECT * FROM CurrentChapterPage where ProfileId=? and ChapterId=?",
                    h, profileId, chapterId);
            if (page == null) {
                return null;
            }
            return page.getPageSequence();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return 0;
    }

    public void updatePage(String openid, Integer profileId, int chapterId, int pageSequence) {
        QueryRunner run = new QueryRunner(getDataSource());

        Integer value = currentPage(profileId, chapterId);
        if (value == null) {
            insert(openid, profileId, chapterId, pageSequence, run);
        } else {
            String updateSql = "UPDATE CurrentChapterPage SET PageSequence=? WHERE Openid=? AND ChapterId=? ";
            try {
                run.update(updateSql,
                        pageSequence, openid, chapterId);
            } catch (SQLException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
    }

    public int insert(String openid, Integer profileId, int chapterId, int pageSequence, QueryRunner run) {
        String insertSql = "INSERT INTO CurrentChapterPage(Openid, ProfileId, ChapterId, PageSequence) " +
                "VALUES(?, ?, ?, ?)";
        try {
            Integer result = run.update(insertSql,
                    openid, profileId, chapterId, pageSequence);
            return result;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }


    public List<CurrentChapterPage> currentPages(Integer profileId, List<Integer> chapterIds) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<CurrentChapterPage>> h = new BeanListHandler<>(CurrentChapterPage.class);

        String questionMark = produceQuestionMark(chapterIds.size());
        List<Object> objects = Lists.newArrayList();
        objects.add(profileId);
        objects.addAll(chapterIds);
        try {
            List<CurrentChapterPage> pages = run.query("SELECT * FROM CurrentChapterPage where ProfileId=? and ChapterId in (" + questionMark + ")",
                    h, objects.toArray());

            return pages;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }
}
