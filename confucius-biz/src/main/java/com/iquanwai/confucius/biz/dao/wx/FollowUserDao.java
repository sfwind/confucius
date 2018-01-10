package com.iquanwai.confucius.biz.dao.wx;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.Account;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 16/8/12.
 */
@Repository
public class FollowUserDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(Account account) {
        QueryRunner run = new QueryRunner(getDataSource());
        String sql = "INSERT INTO FollowUsers(Openid, WeMiniOpenId, UnionId, Nickname, Sex, City, Country, Province, Headimgurl, Subscribe_time, Remark, Groupid)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            return run.update(sql,
                    account.getOpenid(),
                    account.getWeMiniOpenId(),
                    account.getUnionid(),
                    account.getNickname(),
                    account.getSex(),
                    account.getCity(),
                    account.getCountry(),
                    account.getProvince(),
                    account.getHeadimgurl(),
                    account.getSubscribe_time(),
                    account.getRemark(),
                    account.getGroupid());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }

    public Account queryByOpenid(String openid) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<Account> h = new BeanHandler<>(Account.class);

        try {
            Account account = run.query("SELECT * FROM FollowUsers where Openid=?", h, openid);
            return account;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public Account queryByUnionId(String unionId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM FollowUsers WHERE UnionId = ?";
        ResultSetHandler<Account> h = new BeanHandler<>(Account.class);
        try {
            return runner.query(sql, h, unionId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<String> queryAll() {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<String>> h = new ColumnListHandler<>();

        try {
            List<String> account = run.query("SELECT OpenId FROM FollowUsers", h);
            return account;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public void updateMeta(Account account) {
        QueryRunner run = new QueryRunner(getDataSource());
        String updateSql = "Update FollowUsers Set Nickname=?, Headimgurl=?, UnionId=?, Subscribe=? where Openid=?";
        try {
            run.update(updateSql,
                    account.getNickname(), account.getHeadimgurl(),
                    account.getUnionid(), account.getSubscribe(), account.getOpenid());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateInfo(Account account) {
        QueryRunner run = new QueryRunner(getDataSource());
        String updateSql = "Update FollowUsers Set MobileNo=?, Email=?, Industry=?, Function=?, WorkingLife=?, " +
                "RealName=?, City=?, Province=? where Openid=?";
        try {
            run.update(updateSql,
                    account.getMobileNo(), account.getEmail(),
                    account.getIndustry(), account.getFunction(),
                    account.getWorkingLife(), account.getRealName(),
                    account.getCity(), account.getProvince(),
                    account.getOpenid());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void unsubscribe(String openid) {
        QueryRunner run = new QueryRunner(getDataSource());
        String updateSql = "Update FollowUsers Set Subscribe=0 where Openid=?";
        try {
            run.update(updateSql, openid);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}
