package com.iquanwai.confucius.biz.dao.common.permission;


import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.apply.AuditionReward;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.util.page.Page;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.omg.CORBA.PUBLIC_MEMBER;
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
        String sql = " select * from UserRole where roleId in (3,4,5,6,11,12,13,14,15) and Del = 0 ";
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
        return -1;
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
        String sql = " select * from UserRole where profileId = ? And RoleId in (3,4,5,6,11,12,13,14,15) And del = 0";

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


    public List<UserRole> loadAssistsList(Page page){
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<UserRole>> h = new BeanListHandler<>(UserRole.class);
        String sql = "select * from UserRole where RoleId in (3,4,5,6,11,12,13,14,15) and del = 0 LIMIT " + page.getOffset() + "," + page.getLimit();

        try {
            return runner.query(sql,h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return Lists.newArrayList();
    }

    public Integer loadAssistsCount(){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT COUNT(*) FROM UserRole where RoleId in (3,4,5,6,11,12,13,14,15) and del = 0 ";

        try {
            return runner.query(sql,new ScalarHandler<Long>()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return -1;
    }

    public List<UserRole> loadSearchAssists(List<Integer> profiles){
        if (profiles.size() == 0) {
            return Lists.newArrayList();
        }

        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM UserRole WHERE ProfileId IN (" + produceQuestionMark(profiles.size()) + ") AND Del = 0";
        ResultSetHandler<List<UserRole>> h = new BeanListHandler<>(UserRole.class);
        try {
            return runner.query(sql, h, profiles.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
