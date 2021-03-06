package com.iquanwai.confucius.biz.dao.wx;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.Account;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 16/8/12.
 */
@Repository
public class FollowUserDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(Account account) {
        QueryRunner run = new QueryRunner(getDataSource());
        String sql = "INSERT INTO FollowUsers(Openid, WeMiniOpenId, UnionId, Nickname, Sex, City, Country, Province, Headimgurl, Subscribe, Subscribe_time, Remark, Groupid)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Long result = run.insert(sql, new ScalarHandler<>(),
                    account.getOpenid(),
                    account.getWeMiniOpenId(),
                    account.getUnionid(),
                    account.getNickname(),
                    account.getSex(),
                    account.getCity(),
                    account.getCountry(),
                    account.getProvince(),
                    account.getHeadimgurl(),
                    account.getSubscribe(),
                    account.getSubscribe_time(),
                    account.getRemark(),
                    account.getGroupid());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }

    public int updateOAuthFields(Account account) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE FollowUsers SET OpenId = ?, WeMiniOpenId = ?, UnionId = ?, NickName = ?, Sex = ?, City = ?, Country = ?, " +
                "Province = ?, HeadImgUrl = ?, Remark = ?, GroupId = ?, Subscribe = ?, Subscribe_time = ? WHERE UnionId = ?";
        try {
            return runner.update(sql, account.getOpenid(), account.getWeMiniOpenId(), account.getUnionid(), account.getNickname(),
                    account.getSex(), account.getCity(), account.getCountry(), account.getProvince(), account.getHeadimgurl(),
                    account.getRemark(), account.getGroupid(), account.getSubscribe(), account.getSubscribe_time(),
                    account.getUnionid());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Account queryByOpenid(String openid) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<Account> h = new BeanHandler<>(Account.class);

        try {
            return run.query("SELECT * FROM FollowUsers where Openid = ? AND Del = 0", h, openid);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public Account queryByUnionId(String unionId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM FollowUsers WHERE UnionId = ? AND Del = 0";
        ResultSetHandler<Account> h = new BeanHandler<>(Account.class);

        try {
            return runner.query(sql, h, unionId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public void unsubscribe(String openid) {
        QueryRunner run = new QueryRunner(getDataSource());
        String updateSql = "Update FollowUsers Set Subscribe=0 where Openid=? AND Del = 0";
        try {
            run.update(updateSql, openid);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}
