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
        String insertSql = "INSERT INTO FollowUsers(Openid, Country, Groupid, Headimgurl, " +
                "Nickname, Remark, Sex, Subscribe_time, UnionId) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            return run.update(insertSql,
                    account.getOpenid(), account.getCountry(),
                    account.getGroupid(), account.getHeadimgurl(),
                    account.getNickname(), account.getRemark(),
                    account.getSex(), account.getSubscribe_time(), account.getUnionid());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }

    public Account queryByOpenid(String openid) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<Account> h = new BeanHandler(Account.class);

        try {
            Account account = run.query("SELECT * FROM FollowUsers where Openid=?", h, openid);
            return account;
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
        String updateSql = "Update FollowUsers Set Nickname=?, Headimgurl=?, UnionId = ? where Openid=?";
        try {
            run.update(updateSql,
                    account.getNickname(), account.getHeadimgurl(), account.getUnionid(), account.getOpenid());
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

}
