package com.iquanwai.confucius.biz.dao.operational;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.PromoCodeUsage;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 17/2/14.
 */
@Repository
public class PromoCodeUsageDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void insert(PromoCodeUsage promoCodeUsage) {
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "INSERT INTO PromoCodeUsage(User, PromoCodeId) " +
                "VALUES(?, ?)";
        try {
            run.insert(insertSql, new ScalarHandler<>(),
                    promoCodeUsage.getUser(), promoCodeUsage.getPromoCodeId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
