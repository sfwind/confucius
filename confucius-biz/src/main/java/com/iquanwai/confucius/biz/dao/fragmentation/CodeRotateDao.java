package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.CodeRotate;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class CodeRotateDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<CodeRotate> loadAllCodeRotates() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM CodeRotate WHERE Del = 0";
        ResultSetHandler<List<CodeRotate>> h = new BeanListHandler<>(CodeRotate.class);
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<CodeRotate> loadBySceneCode(String sceneCode) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM CodeRotate WHERE SceneCode = ? AND Del = 0";
        ResultSetHandler<List<CodeRotate>> h = new BeanListHandler<>(CodeRotate.class);
        try {
            return runner.query(sql, h, sceneCode);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
