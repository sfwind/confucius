package com.iquanwai.confucius.biz.dao;

import com.iquanwai.confucius.biz.po.WhiteList;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 16/12/26.
 */
@Repository
public class WhiteListDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public WhiteList loadWhiteList(String function, String openid){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<WhiteList> h = new BeanHandler(WhiteList.class);
        String sql = "SELECT * FROM WhiteList where Function=? and Openid=?";
        try {
            WhiteList whiteList = run.query(sql, h, function, openid);
            return whiteList;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }
}