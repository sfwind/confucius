package com.iquanwai.confucius.biz.dao.performance;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.performance.PersonalPerformance;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by yongqiang.shen on 17/3/8.
 */
@Repository
public class PersonalPerformanceDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void entry(PersonalPerformance personalPerformance) {
        if (personalPerformance.getKey() == null || "".equals(personalPerformance.getKey())) {
            return;
        }
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "INSERT INTO PersonalPerformance(`Key`, Name, `Time`) " +
                "VALUES(?, ?, ?)";

        try {
            run.insert(insertSql, new ScalarHandler<>(),
                    personalPerformance.getKey(), personalPerformance.getName(),
                    personalPerformance.getTime());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<PersonalPerformance> queryAboutAddTime(String key,String startTimeStr, String endTimeStr){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<PersonalPerformance>> h = new BeanListHandler<>(PersonalPerformance.class);
        String sql = "SELECT * FROM PersonalPerformance where `Key` = ? and AddTime >= ? and AddTime <= ? order by AddTime";
        try {
            return run.query(sql, h, key, startTimeStr, endTimeStr);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
