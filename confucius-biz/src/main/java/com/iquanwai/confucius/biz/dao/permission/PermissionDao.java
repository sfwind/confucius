package com.iquanwai.confucius.biz.dao.permission;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.permisson.Permission;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2016/12/28.
 */
@Repository
public class PermissionDao extends DBUtil{

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<Permission> loadPermissions(Integer roleId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM Permission WHERE RoleId = ?";
        ResultSetHandler<List<Permission>> h = new BeanListHandler(Permission.class);
        try {
            return runner.query(sql, h, roleId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
