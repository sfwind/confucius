package com.iquanwai.confucius.biz.dao.common.permission;


import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2016/12/29.
 */
@Repository
public class UserRoleDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<UserRole> getRoles(Integer profileId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<UserRole>> h = new BeanListHandler<>(UserRole.class);
        String sql = "SELECT * FROM UserRole where ProfileId=? and Del=0";
        try {
            return run.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    /**
     * 获得所有的教练
     *
     * @return
     */
    public List<UserRole> loadAssists() {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<UserRole>> h = new BeanListHandler<>(UserRole.class);
        String sql = " select * from UserRole where roleId in (3,4,11) and Del = 0 ";

        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    /**
     * 修改教练状态
     *
     * @param id
     * @param roleId
     * @return
     */
    public Integer updateAssist(Integer id, Integer roleId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update UserRole set RoleId = ? where id = ?";
        try {
            return runner.update(sql, roleId, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return 0;
    }

    /**
     * 教练过期
     *
     * @param id
     */
    public Integer deleteAssist(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update UserRole set Del = 1 where id = ?";

        try {
            return runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return 0;
    }

    /**
     * 加载
     *
     * @param profileId
     * @return
     */
    public UserRole loadAssist(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<UserRole> h = new BeanHandler<>(UserRole.class);
        String sql = " select * from UserRole where profileId = ? And RoleId in (3,4,11) And del = 0";

        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * 添加教练
     *
     * @param roleId
     * @param profileId
     * @return
     */
    public Integer insertAssist(Integer roleId, Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = " insert into UserRole(RoleId,ProfileId) values(?,?)";

        try {
            Long result = runner.insert(sql, new ScalarHandler<>(), roleId, profileId);
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
