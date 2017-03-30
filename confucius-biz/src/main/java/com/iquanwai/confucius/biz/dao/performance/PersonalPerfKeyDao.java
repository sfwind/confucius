package com.iquanwai.confucius.biz.dao.performance;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.performance.PersonalPerfKey;
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
 * Created by shen on 17/3/8.
 */
@Repository
public class PersonalPerfKeyDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void entry(PersonalPerfKey personalPerfKey) {
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "INSERT INTO PersonalPerfKey(`Key`) " + "VALUES(?)";
        try {
            run.insert(insertSql, new ScalarHandler<>(), personalPerfKey.getKey());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public PersonalPerfKey getById(int id) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<PersonalPerfKey> h = new BeanHandler<>(PersonalPerfKey.class);
        String sql = "SELECT * FROM PersonalPerfKey where Id = ?";
        try {
            return run.query(sql, h, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public PersonalPerfKey getByKey(String key) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<PersonalPerfKey> h = new BeanHandler<>(PersonalPerfKey.class);
        String sql = "SELECT * FROM PersonalPerfKey where `Key` = ? Limit 1";
        try {
            return run.query(sql, h, key);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<PersonalPerfKey> queryAll() {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<PersonalPerfKey>> h = new BeanListHandler<>(PersonalPerfKey.class);
        String sql = "SELECT * FROM PersonalPerfKey";
        try {
            return run.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
