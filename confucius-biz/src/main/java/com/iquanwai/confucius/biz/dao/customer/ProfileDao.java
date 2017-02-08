package com.iquanwai.confucius.biz.dao.customer;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.customer.Profile;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.concurrent.Executors;

/**
 * Created by nethunder on 2017/2/8.
 */
@Repository
public class ProfileDao extends DBUtil{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Profile queryByOpenId(String openId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<Profile> h = new BeanHandler(Profile.class);

        try {
            return run.query("SELECT * FROM Profile where Openid=?", h, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public boolean submitPersonalCenterProfile(Profile profile) {
        QueryRunner run = new QueryRunner(getDataSource());
        String updateSql = "Update Profile Set Industry=?, Function=?, WorkingLife=?, City=?, Province=? where Openid=?";
        try {
            run.update(updateSql,
                    profile.getIndustry(),
                    profile.getFunction(),
                    profile.getWorkingLife(),
                    profile.getCity(),
                    profile.getProvince(),
                    profile.getOpenid());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }


    public int insertProfile(Profile profile) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO Profile(Openid, Nickname, City, Country, Province, Headimgurl, MobileNo, Email, Industry, Function, WorkingLife, RealName)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    profile.getOpenid(), profile.getNickname(),profile.getCity(),profile.getCountry(),profile.getProvince(),
                    profile.getHeadimgurl(),profile.getMobileNo(),profile.getEmail(),profile.getIndustry(),
                    profile.getFunction(),profile.getWorkingLife(),profile.getRealName());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public void updatePoint(String openId, int point) {
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "UPDATE Profile SET Point = ? where Openid = ?";
        try {
            asyncRun.update(sql, point, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void completeProfile(String openId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "UPDATE Profile SET IsFull = 1 where Openid = ?";
        try {
            asyncRun.update(sql, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
