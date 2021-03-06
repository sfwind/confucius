package com.iquanwai.confucius.biz.dao.common.customer;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.common.message.ShortMessageSubmit;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/6/18.
 */
@Repository
public class ShortMessageSubmitDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public int insert(ShortMessageSubmit shortMessageSubmit){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO ShortMessageSubmit(MsgId, ProfileId, Phones, Content, Sign, SendTime, Result, Description,FailPhones,Type)  " +
                "    VALUE (?,?,?,?,?,?,?,?,?,?)";
        try{
            return runner.insert(sql, new ScalarHandler<Long>(),
                    shortMessageSubmit.getMsgId(), shortMessageSubmit.getProfileId(), shortMessageSubmit.getPhones(),
                    shortMessageSubmit.getContent(), shortMessageSubmit.getSign(), shortMessageSubmit.getSendTime(),
                    shortMessageSubmit.getResult(), shortMessageSubmit.getDescription(), shortMessageSubmit.getFailPhones(),
                    shortMessageSubmit.getType()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
