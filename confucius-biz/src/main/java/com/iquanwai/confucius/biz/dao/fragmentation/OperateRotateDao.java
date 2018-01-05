package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.OperateRotate;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class OperateRotateDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<OperateRotate> loadAllOperateRotates() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM OperateRotate WHERE Del = 0";
        ResultSetHandler<List<OperateRotate>> h = new BeanListHandler<>(OperateRotate.class);
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<OperateRotate> loadBySceneCode(String sceneCode) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM OperateRotate WHERE SceneCode = ? AND Del = 0";
        ResultSetHandler<List<OperateRotate>> h = new BeanListHandler<>(OperateRotate.class);
        try {
            return runner.query(sql, h, sceneCode);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
