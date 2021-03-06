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

    public int insert(Callback callback) {
        QueryRunner run = new QueryRunner(getDataSource());
        String sql = "INSERT INTO Callback(State, CallbackUrl, AccessToken, PcAccessToken, WeMiniAccessToken, " +
                "RefreshToken, UnionId, Openid, PcOpenid, WeMiniOpenid) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Long result = run.insert(sql, new ScalarHandler<>(),
                    callback.getState(),
                    callback.getCallbackUrl(),
                    callback.getAccessToken(),
                    callback.getPcAccessToken(),
                    callback.getWeMiniAccessToken(),
                    callback.getRefreshToken(),
                    callback.getUnionId(),
                    callback.getOpenid(),
                    callback.getPcOpenid(),
                    callback.getWeMiniOpenid());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public int updateFields(Callback callback) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE Callback SET State = ?, CallbackUrl = ?, AccessToken = ?, PcAccessToken = ?, WeMiniAccessToken = ?, " +
                "RefreshToken = ?, UnionId = ?, OpenId = ?, PcOpenId = ?, WeMiniOpenId = ? WHERE Id = ?";
        try {
            return runner.update(sql, callback.getState(), callback.getCallbackUrl(), callback.getAccessToken(), callback.getPcAccessToken(),
                    callback.getWeMiniAccessToken(), callback.getRefreshToken(), callback.getUnionId(), callback.getOpenid(), callback.getPcOpenid(),
                    callback.getWeMiniOpenid(), callback.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public void refreshToken(String state, String newAccessToken) {
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
            return run.query("SELECT * FROM Callback where State = ?", h, state);
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

    public Callback queryByUnionId(String unionId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM Callback WHERE UnionId = ?";
        ResultSetHandler<Callback> h = new BeanHandler<>(Callback.class);
        try {
            return runner.query(sql, h, unionId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
