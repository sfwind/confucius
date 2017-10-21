package com.iquanwai.confucius.biz.dao.common.customer;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.PromotionUser;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 17/5/31.
 */
@Repository
public class PromotionUserDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public int insert(PromotionUser promotionUser){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into PromotionUser(Openid, Source, Action, ProfileId) " +
                " VALUES (?, ?, ?, ?)";

        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    promotionUser.getOpenid(),promotionUser.getSource(),
                    promotionUser.getAction(), promotionUser.getProfileId());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public PromotionUser loadPromotion(String openid, String source){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from PromotionUser where OpenId = ? and Source=?";

        try{
            ResultSetHandler<PromotionUser> handler = new BeanHandler<>(PromotionUser.class);
            return runner.query(sql, handler, openid, source);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
