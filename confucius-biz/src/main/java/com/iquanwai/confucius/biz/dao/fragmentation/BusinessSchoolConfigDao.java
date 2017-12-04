package com.iquanwai.confucius.biz.dao.fragmentation;

import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.BusinessSchoolConfig;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class BusinessSchoolConfigDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public BusinessSchoolConfig loadActiveConfig() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM BusinessSchoolConfig WHERE Active = 1";
        ResultSetHandler<BusinessSchoolConfig> h = new BeanHandler<>(BusinessSchoolConfig.class);
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
