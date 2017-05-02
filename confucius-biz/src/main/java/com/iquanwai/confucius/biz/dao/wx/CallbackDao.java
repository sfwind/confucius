package com.iquanwai.confucius.biz.dao.wx;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.Callback;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 16/8/13.
 */
@Repository
public class CallbackDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void insert(Callback callback) {
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "INSERT INTO Callback(Openid, Accesstoken, CallbackUrl, RefreshToken, State) " +
                "VALUES(?, ?, ?, ?, ?)";
        try {
            run.insert(insertSql, new ScalarHandler<>(),
                    callback.getOpenid(), callback.getAccessToken(), callback.getCallbackUrl(),
                    callback.getRefreshToken(), callback.getState());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

    }

    public void updateUserInfo(String state,
                               String accessToken,
                               String refreshToken,
                               String openid) {
        QueryRunner run = new QueryRunner(getDataSource());
        try {
            run.update("UPDATE Callback Set AccessToken=?,RefreshToken=?,Openid=? where State=?",
                    accessToken, refreshToken, openid, state);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void refreshToken(String state,
                               String newAccessToken) {
        QueryRunner run = new QueryRunner(getDataSource());
        try {
            run.update("UPDATE Callback Set AccessToken=? where State=?",
                    newAccessToken, state);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public Callback queryByState(String state) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<Callback> h = new BeanHandler<>(Callback.class);

        try {
            Callback callback = run.query("SELECT * FROM Callback where State=?", h, state);
            return callback;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public Callback queryByAccessToken(String accessToken) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<Callback> h = new BeanHandler<>(Callback.class);

        try {
            Callback callback = run.query("SELECT * FROM Callback where AccessToken=?", h, accessToken);
            return callback;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

}
