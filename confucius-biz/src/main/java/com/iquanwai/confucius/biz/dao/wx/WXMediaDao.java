package com.iquanwai.confucius.biz.dao.wx;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.WXMedia;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class WXMediaDao extends DBUtil{

    private Logger logger = LoggerFactory.getLogger(getClass());

    public Integer insert(WXMedia wxMedia){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into WXMedia(MediaId,Url,Remark) values (?,?,?)";

        try {
           Long result = runner.insert(sql,new ScalarHandler<>(),wxMedia.getMediaId(),wxMedia.getUrl(),wxMedia.getRemark());
           return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return -1;
    }
}
