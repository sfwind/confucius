package com.iquanwai.confucius.biz.dao.wx;


import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.PromotionQrCode;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class PromotionCodeDao extends DBUtil{

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public PromotionQrCode getByScene(String scene){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM PromotionQrCode WHERE SCENE = ? AND DEL = 0";
        ResultSetHandler<PromotionQrCode> h = new BeanHandler<>(PromotionQrCode.class);
        try {
            return runner.query(sql,h,scene);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return null;
    }


    public Integer insert(PromotionQrCode promotionQrCode){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO PromotionQrCode(Scene,Url,Remark) VALUES(?,?,?) ";

        try {
            Long result =  runner.insert(sql,new ScalarHandler<>(),promotionQrCode.getScene(),promotionQrCode.getUrl(),promotionQrCode.getRemark());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return -1;
    }
}
