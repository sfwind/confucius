package com.iquanwai.confucius.biz.dao.operational;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.systematism.Angel;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by justin on 16/10/11.
 */
@Repository
public class AngelDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(Angel angel) {
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);
        String insertSql = "INSERT INTO Angel(MemberId, AngelId, ClassId) " +
                "VALUES(?, ?, ?)";
        try {
            Future<Integer> result = asyncRun.update(insertSql,
                    angel.getMemberId(), angel.getAngelId(), angel.getClassId());
            return result.get();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            // ignore
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
        }

        return -1;
    }
}
