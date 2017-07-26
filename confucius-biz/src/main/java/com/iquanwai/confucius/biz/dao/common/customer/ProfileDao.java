package com.iquanwai.confucius.biz.dao.common.customer;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.exception.ErrorConstants;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/2/8.
 */
@Repository
public class ProfileDao extends DBUtil{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Profile queryByUnionId(String unionId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<Profile> h = new BeanHandler<Profile>(Profile.class);
        try{
            return run.query("SELECT * FROM Profile where UnionId=?", h, unionId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public Profile queryByOpenId(String openId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<Profile> h = new BeanHandler<>(Profile.class);

        try {
            return run.query("SELECT * FROM Profile where Openid=?", h, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public void updateMeta(Profile profile) {
        QueryRunner run = new QueryRunner(getDataSource());
        String updateSql = "Update Profile Set Nickname=?, Headimgurl=?, UnionId = ? where Openid=?";
        try {
            run.update(updateSql,
                    profile.getNickname(), profile.getHeadimgurl(), profile.getUnionid(), profile.getOpenid());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
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

    public int insertProfile(Profile profile) throws SQLException {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO Profile(Openid, Nickname, City, Country, Province, Headimgurl, MobileNo, Email, Industry, Function, WorkingLife, RealName, RiseId, UnionId)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    profile.getOpenid(), profile.getNickname(),profile.getCity(),profile.getCountry(),profile.getProvince(),
                    profile.getHeadimgurl(),profile.getMobileNo(),profile.getEmail(),profile.getIndustry(),
                    profile.getFunction(),profile.getWorkingLife(),profile.getRealName(),profile.getRiseId(), profile.getUnionid());
            return insertRs.intValue();
        } catch (SQLException e) {
            if (e.getErrorCode() == ErrorConstants.DUPLICATE_CODE) {
                throw e;
            }
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public void updatePoint(String openId, int point) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE Profile SET Point = ? where Openid = ?";
        try {
            runner.update(sql, point, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateRiseMember(String openId, int riseMember) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE Profile SET RiseMember = ? where OpenId = ?";
        try {
            runner.update(sql, riseMember, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void completeProfile(String openId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE Profile SET IsFull = 1 where Openid = ?";
        try {
            runner.update(sql, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public Boolean submitPersonalProfile(Profile account) {
        QueryRunner run = new QueryRunner(getDataSource());
        String updateSql = "Update Profile Set MobileNo=?, Email=?, Industry=?, Function=?, WorkingLife=?, " +
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
            return false;
        }
        return true;
    }

    public void becomeRiseMember(String openId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update Profile SET RiseMember = 1 WHERE OpenId = ?";
        try{
            runner.update(sql, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void becomeRiseEliteMember(String openId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update Profile SET RiseMember = 1, RequestCommentCount=1 WHERE OpenId = ?";
        try{
            runner.update(sql, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public Integer riseMemberCount(){
        QueryRunner runner = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<Long>();
        String sql = "Select Count(1) from Profile where RiseMember = 1";
        try{
            return runner.query(sql,h).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }


}
