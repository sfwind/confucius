package com.iquanwai.confucius.biz.dao;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Created by justin on 16/8/29.
 */
@Repository
public class DBUtil {
    @Autowired
    @Qualifier("dao.dataSource")
    private DataSource ds;

    private Logger logger = LoggerFactory.getLogger(getClass());

    protected DataSource getDataSource(){
        return ds;
    }

    public <T> T load(Class<T> type, int id){

        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<T> h = new BeanHandler<T>(type);

        try {
            T t = run.query("SELECT * FROM "+type.getSimpleName()+" where id=?", h, id);
            return t;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }


    public int count(Class type){

        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<Integer> h = new BeanHandler<Integer>(type);

        try {
            Integer number = run.query("SELECT count(*) FROM "+type.getSimpleName(), h);
            return number;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }

}
