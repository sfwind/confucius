package com.iquanwai.confucius.biz.dao.operational;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.systematism.Angel;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 16/10/11.
 */
@Repository
public class AngelDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void insert(Angel angel) {
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "INSERT INTO Angel(MemberId, AngelId, ClassId) " +
                "VALUES(?, ?, ?)";
        try {
            run.insert(insertSql, new ScalarHandler<>(),
                    angel.getMemberId(), angel.getAngelId(), angel.getClassId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
