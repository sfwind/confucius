package com.iquanwai.confucius.biz.dao.performance;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.performance.PageUrl;
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
 * Created by shen on 17/3/6.
 */
@Repository
public class PageUrlDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void entry(PageUrl pageUrl) {
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "INSERT INTO PageUrl(Url) " + "VALUES(?)";
        try {
            run.insert(insertSql, new ScalarHandler<>(), pageUrl.getUrl());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public PageUrl getById(int id) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<PageUrl> h = new BeanHandler<>(PageUrl.class);
        String sql = "SELECT * FROM PageUrl where Id = ?";
        try {
            return run.query(sql, h, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public PageUrl getByUrl(String url) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<PageUrl> h = new BeanHandler<>(PageUrl.class);
        String sql = "SELECT * FROM PageUrl where Url = ? Limit 1";
        try {
            return run.query(sql, h, url);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<PageUrl> queryAll(){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<PageUrl>> h = new BeanListHandler<>(PageUrl.class);
        String sql = "SELECT * FROM PageUrl";
        try {
            return run.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
