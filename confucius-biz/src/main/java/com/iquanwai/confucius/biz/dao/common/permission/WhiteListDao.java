package com.iquanwai.confucius.biz.dao.common.permission;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.WhiteList;
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
 * Created by justin on 16/12/26.
 */
@Repository
public class WhiteListDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public WhiteList loadWhiteList(String function, Integer profileId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<WhiteList> h = new BeanHandler<>(WhiteList.class);
        String sql = "SELECT * FROM WhiteList where Function=? and ProfileId=? and Del=0";
        try {
            WhiteList whiteList = run.query(sql, h, function, profileId);
            return whiteList;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }


    public List<WhiteList> loadWhiteList(String function) {
        QueryRunner run = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM WhiteList where Function=? and Del=0";
        try {
            return run.query(sql, new BeanListHandler<>(WhiteList.class), function);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

}
