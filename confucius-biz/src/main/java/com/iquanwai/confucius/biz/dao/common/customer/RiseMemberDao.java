package com.iquanwai.confucius.biz.dao.common.customer;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
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
 * Created by nethunder on 2017/4/13.
 */
@Repository
public class RiseMemberDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void updateExpiredAhead(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE RiseMember SET Expired = 1, Memo = '商学院提前过期' WHERE ProfileId = ? AND Expired = 0";
        try {
            runner.update(sql, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public int insert(RiseMember riseMember) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into RiseMember(Openid, ProfileId, OrderId, MemberTypeId, ExpireDate) " +
                " VALUES (?, ?, ?, ?, ?)";

        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    riseMember.getOpenId(), riseMember.getProfileId(), riseMember.getOrderId(),
                    riseMember.getMemberTypeId(), riseMember.getExpireDate());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public RiseMember loadValidRiseMember(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseMember where ProfileId = ? and expired = 0";

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
        String sql = "select * from RiseMember where OrderId = ?";

        try {
            ResultSetHandler<RiseMember> handler = new BeanHandler<>(RiseMember.class);
            return runner.query(sql, handler, orderId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<RiseMember> eliteMembers() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseMember where MemberTypeId = 3 and expired = 0";

        try {
            ResultSetHandler<List<RiseMember>> handler = new BeanListHandler<>(RiseMember.class);
            return runner.query(sql, handler);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
