package com.iquanwai.confucius.biz.dao.common.customer;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.common.customer.CustomerStatus;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/9/6.
 */
@Repository
public class CustomerStatusDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public CustomerStatus load(Integer profileId, Integer statusId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM CustomerStatus WHERE ProfileId = ? and StatusId = ? and Del = 0";
        try {
            return runner.query(sql, new BeanHandler<>(CustomerStatus.class), profileId, statusId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public Integer insert(Integer profileId, Integer statusId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO CustomerStatus(ProfileId, StatusId, Del) VALUES(?,?,0)";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(), profileId, statusId).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public void delStatus(Integer profileId, Integer statusId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update CustomerStatus set Del=1 where ProfileId=? and StatusId=?";
        try {
            runner.update(sql, profileId, statusId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}


