package com.iquanwai.confucius.biz.dao.wx;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.Account;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by justin on 16/8/12.
 */
@Repository
public class FollowUserDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(Account account) {
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);
        String insertSql = "INSERT INTO FollowUsers(Openid, City, Country, Groupid, Headimgurl, Language, " +
                "Nickname, Province, Remark, Sex, Subscribe, Subscribe_time) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Future<Integer> result = asyncRun.update(insertSql,
                    account.getOpenid(), account.getCity(), account.getCountry(),
                    account.getGroupid(), account.getHeadimgurl(), account.getLanguage(),
                    account.getNickname(), account.getProvince(), account.getRemark(),
                    account.getSex(), account.getSubscribe(), account.getSubscribe_time());
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
}
