package com.iquanwai.confucius.biz.dao.common.message;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.domain.message.MQDealLog;
import com.iquanwai.confucius.biz.util.ThreadPool;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by nethunder on 2017/7/24.
 */
@Repository
public class MQDealLogDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public int insert(MQDealLog message){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(ThreadPool.getThreadExecutor(), run);
        try {
            String insertSql = "INSERT INTO MQDealLog(MsgId, Topic, Queue, ConsumerIp)  VALUES (?,?,?,?)";
            Future<Integer> result = asyncRun.update(insertSql, message.getMsgId(),
                    message.getTopic(), message.getQueue(), message.getConsumerIp());
            return result.get();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            // ignore
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }
}
