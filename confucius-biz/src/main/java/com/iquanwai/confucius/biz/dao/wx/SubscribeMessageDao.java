package com.iquanwai.confucius.biz.dao.wx;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.SubscribeMessage;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 17/7/8.
 */
@Repository
public class SubscribeMessageDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<SubscribeMessage> loadSubscribeMessages() {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<SubscribeMessage>> h = new BeanListHandler<>(SubscribeMessage.class);

        try {
            List<SubscribeMessage> messages =
                    run.query("SELECT * FROM SubscribeMessage where Event=1 and Del=0 and Channel is null", h);
            return messages;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public List<SubscribeMessage> loadSubscribeMessages(String channel) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<SubscribeMessage>> h = new BeanListHandler<>(SubscribeMessage.class);

        try {
            List<SubscribeMessage> messages =
                    run.query("SELECT * FROM SubscribeMessage where Channel=? and Event=1 and Del=0", h, channel);
            return messages;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public List<SubscribeMessage> loadSubscribeDefaultTextMessages() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM SubscribeMessage WHERE Event = 1 AND Channel IS NULL AND Type = 1";
        ResultSetHandler<List<SubscribeMessage>> h = new BeanListHandler<>(SubscribeMessage.class);
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<SubscribeMessage> loadScanMessages() {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<SubscribeMessage>> h = new BeanListHandler<>(SubscribeMessage.class);

        try {
            List<SubscribeMessage> messages =
                    run.query("SELECT * FROM SubscribeMessage where Event=2 and Del=0 and Channel is null", h);
            return messages;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public List<SubscribeMessage> loadScanMessages(String channel) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<SubscribeMessage>> h = new BeanListHandler<>(SubscribeMessage.class);

        try {
            List<SubscribeMessage> messages =
                    run.query("SELECT * FROM SubscribeMessage where Channel=? and Event=2 and Del=0", h, channel);
            return messages;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public int updateSubscribeDefaultTextMessage(SubscribeMessage message) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE SubscribeMessage SET Message = ?, Del = ? " +
                "WHERE Id = ?";
        try {
            return runner.update(sql, message.getMessage(),
                    message.getDel(),
                    message.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

}
