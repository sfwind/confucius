package com.iquanwai.confucius.biz.dao.common.permission;


import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
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

    public List<UserRole> getRoles(String openid) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<UserRole>> h = new BeanListHandler<>(UserRole.class);
        String sql = "SELECT * FROM UserRole where Openid=? and Del=0";
        try {
            return run.query(sql, h, openid);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }


    /**
     * 更新UserRole
     *
     * @param id
     * @param roleId
     * @param del
     * @return
     */
    public Integer updateRole(Integer id, Integer roleId, boolean del) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update UserRole set roleId =?,del = ? where id = ?";
        try {
            return runner.update(sql, roleId, del, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return 0;
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
     * @param id
     * @param roleId
     * @return
     */
    public Integer updateAssist(Integer id,Integer roleId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update UserRole set RoleId = ? where id = ?";
        try {
            return runner.update(sql,roleId,id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return 0;
    }

    /**
     * 教练过期
     * @param id
     */
    public Integer deleteAssist(Integer id){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update UserRole set Del = 1 where id = ?";

        try {
          return  runner.update(sql,id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return -1;
    }

    /**
     * 加载
     * @param profileId
     * @return
     */
    public UserRole loadAssist(Integer profileId){
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<UserRole> h = new BeanHandler<>(UserRole.class);
        String sql = " select * from UserRole where profileId = ? And RoleId in (3,4,11) And del = 0";

        try {
           return runner.query(sql,h,profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return null;
    }

    /**
     * 添加教练
     * @param roleId
     * @param openId
     * @param profileId
     * @return
     */
    public Integer insertAssist(Integer roleId,String openId,Integer profileId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = " insert into UserRole(RoleId,OpenId,ProfileId) values(?,?,?)";

        try {
          Long result = runner.insert(sql,new ScalarHandler<>(),roleId,openId,profileId);
          return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return -1;
    }


    public List<UserRole> loadAssistsList(Page page){
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<UserRole>> h = new BeanListHandler<>(UserRole.class);
        String sql = "select * from UserRole where RoleId in (3,11,12,13,14,15,16,17) and del = 0 LIMIT " + page.getOffset() + "," + page.getLimit();

        try {
            return runner.query(sql,h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return Lists.newArrayList();
    }

    public Integer loadAssistsCount(){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT COUNT(*) FROM UserRole where RoleId in (3,11,12,13,14,15,16,17) and del = 0 ";

        try {
            return runner.query(sql,new ScalarHandler<Long>()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return -1;
    }
}
