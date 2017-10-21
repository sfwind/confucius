package com.iquanwai.confucius.biz.dao.common.customer;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.exception.ErrorConstants;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2017/2/8.
 */
@Repository
public class ProfileDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Profile queryByUnionId(String unionId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<Profile> h = new BeanHandler<Profile>(Profile.class);
        try {
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

    public Profile queryByRiseId(String riseId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM Profile WHERE RiseId = ?";
        ResultSetHandler<Profile> h = new BeanHandler<>(Profile.class);
        try {
            return runner.query(sql, h, riseId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public int insertProfile(Profile profile) throws SQLException {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO Profile(Openid, Nickname, City, Country, Province, Headimgurl, MobileNo, Email, Industry, Function, WorkingLife, RealName, RiseId, UnionId)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    profile.getOpenid(), profile.getNickname(), profile.getCity(), profile.getCountry(), profile.getProvince(),
                    profile.getHeadimgurl(), profile.getMobileNo(), profile.getEmail(), profile.getIndustry(),
                    profile.getFunction(), profile.getWorkingLife(), profile.getRealName(), profile.getRiseId(), profile.getUnionid());
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


    public List<Profile> queryAccounts(List<Integer> profileIds) {
        if (CollectionUtils.isEmpty(profileIds)) {
            return Lists.newArrayList();
        }
        String questionMarks = produceQuestionMark(profileIds.size());
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Profile>> h = new BeanListHandler<>(Profile.class);
        String sql = "SELECT * FROM Profile where Id in (" + questionMarks + ")";
        try {
            return run.query(sql, h, profileIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public Integer riseMemberCount() {
        QueryRunner runner = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<Long>();
        String sql = "Select Count(1) from Profile where RiseMember = 1";
        try {
            return runner.query(sql, h).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 根据 NickName 模糊查询
     */
    public List<Profile> loadProfilesByNickName(String nickName) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM Profile where NickName like ? limit 200";
        ResultSetHandler<List<Profile>> h = new BeanListHandler<>(Profile.class);
        try {
            return runner.query(sql, h, "%" + nickName + "%");
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
