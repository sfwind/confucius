package com.iquanwai.confucius.biz.dao.wx;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.AutoReplyMessage;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
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

    public List<AutoReplyMessage> loadAllMessages() {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<AutoReplyMessage>> h = new BeanListHandler<>(AutoReplyMessage.class);

        try {
            List<AutoReplyMessage> messages = run.query("SELECT * FROM AutoReplyMessage where Del=0", h);
            return messages;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }
}
