package com.iquanwai.confucius.biz.dao.common.customer;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/4/13.
 */
@Repository
public class RiseMemberDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<RiseMember> loadValidRiseMemberByMemberTypeId(Integer profileId, List<Integer> memberTypes) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String mask = produceQuestionMark(memberTypes.size());
        List<Object> params = Lists.newArrayList();
        params.add(profileId);
        params.addAll(memberTypes);
        String sql = "SELECT * FROM RiseMember WHERE ProfileId = ? AND memberTypeId in ("
                + mask + ") AND Expired=0 AND Del = 0";
        ResultSetHandler<List<RiseMember>> h = new BeanListHandler<>(RiseMember.class);
        try {
            return runner.query(sql, h, params.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public void updateExpiredAhead(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE RiseMember SET Expired = 1, Memo = '商学院提前过期' WHERE ProfileId = ? AND Expired = 0 AND Del = 0";
        try {
            runner.update(sql, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public int insert(RiseMember riseMember) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into RiseMember(ProfileId, OrderId, MemberTypeId, ExpireDate, Expired, Memo, OpenDate, Vip) " +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    riseMember.getProfileId(),
                    riseMember.getOrderId(),
                    riseMember.getMemberTypeId(),
                    riseMember.getExpireDate(),
                    riseMember.getExpired(),
                    riseMember.getMemo(),
                    riseMember.getOpenDate(),
                    riseMember.getVip()
            );
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public RiseMember loadValidRiseMember(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseMember where ProfileId = ? and Expired = 0 AND Del = 0";

        try {
            ResultSetHandler<RiseMember> handler = new BeanHandler<>(RiseMember.class);
            return runner.query(sql, handler, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public RiseMember loadByOrderId(String orderId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseMember where OrderId = ? AND Del = 0";

        try {
            ResultSetHandler<RiseMember> handler = new BeanHandler<>(RiseMember.class);
            return runner.query(sql, handler, orderId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<Integer> loadEliteMembersId() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT DISTINCT(ProfileId) FROM RiseMember WHERE MemberTypeId IN (3, 4) AND Expired = 0 AND Del = 0";
        ColumnListHandler<Integer> handler = new ColumnListHandler<>("ProfileId");
        try {
            return runner.query(sql, handler);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<RiseMember> loadByWillExpired(Date expiredDate) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseMember WHERE Expired = 0 AND ExpireDate < ? AND Del = 0";
        try {
            return runner.query(sql, new BeanListHandler<>(RiseMember.class), expiredDate);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<RiseMember> loadByProfileIds(List<Integer> profileIds) {
        if (profileIds.size() == 0) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseMember WHERE ProfileId IN (" + produceQuestionMark(profileIds.size()) + ") AND Expired = 0 AND Del = 0";
        ResultSetHandler<List<RiseMember>> h = new BeanListHandler<>(RiseMember.class);
        try {
            return runner.query(sql, h, profileIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<RiseMember> loadPersonalAll(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseMember WHERE ProfileId = ? AND Del = 0";
        ResultSetHandler<List<RiseMember>> h = new BeanListHandler<>(RiseMember.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}