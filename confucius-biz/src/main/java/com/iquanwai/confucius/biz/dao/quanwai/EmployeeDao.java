package com.iquanwai.confucius.biz.dao.quanwai;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.quanwai.QuanwaiEmployee;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class EmployeeDao extends DBUtil {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    public List<QuanwaiEmployee>  getEmployees(){
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<QuanwaiEmployee>> h = new BeanListHandler<QuanwaiEmployee>(QuanwaiEmployee.class);
        String sql = "SELECT * FROM QuanwaiEmployee  WHERE DEL = 0";

        try {
            return runner.query(sql,h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return Lists.newArrayList();
    }

}
