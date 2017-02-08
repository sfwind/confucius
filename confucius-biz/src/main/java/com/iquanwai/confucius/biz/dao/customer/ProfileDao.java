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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    public int submitPersonalCenterProfile(Profile profile) {
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);
        String updateSql = "Update Profile Set Industry=?, Function=?, WorkingLife=?, City=?, Province=? where Openid=?";
        try {
            Future<Integer> result = asyncRun.update(updateSql,
                    profile.getIndustry(), profile.getFunction(), profile.getWorkingLife(), profile.getCity(), profile.getProvince(), profile.getOpenid());
            return result.get();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            // ignore
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
        }

        return -1;
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
}
