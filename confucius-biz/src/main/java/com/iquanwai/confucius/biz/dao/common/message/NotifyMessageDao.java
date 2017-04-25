package com.iquanwai.confucius.biz.dao.common.message;

import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.common.message.NotifyMessage;
import com.iquanwai.confucius.biz.util.page.Page;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by justin on 17/2/27.
 */
@Repository
public class NotifyMessageDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void insert(NotifyMessage message){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into NotifyMessage(Message, FromUser, ToUser, Url, SendTime, IsRead, Old)" +
                "values(?,?,?,?,?,?,?)";
        try {
            runner.insert(sql, new ScalarHandler<>(),
                    message.getMessage(), message.getFromUser(), message.getToUser(),
                    message.getUrl(), message.getSendTime(), message.getIsRead(),
                    message.getOld());
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

    }
}
