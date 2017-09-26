package com.iquanwai.confucius.biz.dao.wx;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.AutoReplyMessage;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 17/7/9.
 */
@Repository
public class AutoReplyMessageDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(AutoReplyMessage message) {
        message.setDel(0);
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO AutoReplyMessage (Type, Message, Keyword, Exact, IsDefault, Del)" +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    message.getType(),
                    message.getMessage(),
                    message.getKeyword(),
                    message.getExact(),
                    message.getIsDefault(),
                    message.getDel());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public int update(AutoReplyMessage message) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE AutoReplyMessage SET Type = ?, Message = ?, Keyword = ?, Exact = ?, IsDefault = ? " +
                "WHERE Id = ?";
        try {
            return runner.update(sql, message.getType(),
                    message.getMessage(),
                    message.getKeyword(),
                    message.getExact(),
                    message.getIsDefault(),
                    message.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public int delete(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE AutoReplyMessage SET Del = 1 WHERE Id = ?";
        try {
            return runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<AutoReplyMessage> loadAllTextMessages() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM AutoReplyMessage WHERE Type = 1 AND Del = 0";
        ResultSetHandler<List<AutoReplyMessage>> h = new BeanListHandler<>(AutoReplyMessage.class);
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<AutoReplyMessage> loadAllMessages() {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<AutoReplyMessage>> h = new BeanListHandler<>(AutoReplyMessage.class);
        try {
            return run.query("SELECT * FROM AutoReplyMessage where Del = 0", h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
