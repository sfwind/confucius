package com.iquanwai.confucius.biz.dao.fragmentation;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.RiseOrder;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/4/6.
 */
@Repository
public class RiseOrderDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(RiseOrder riseOrder) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into RiseOrder(OrderId, ProfileId, MemberType, Entry) " +
                " VALUES (?, ?, ?, ?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    riseOrder.getOrderId(), riseOrder.getProfileId(),
                    riseOrder.getMemberType(), riseOrder.getEntry());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }


    public RiseOrder loadOrder(String orderId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<RiseOrder> h = new BeanHandler<>(RiseOrder.class);

        try {
            RiseOrder order = run.query("SELECT * FROM RiseOrder where OrderId=? ", h, orderId);
            return order;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public void entry(String orderId) {
        QueryRunner run = new QueryRunner(getDataSource());
        String sql = "Update RiseOrder SET Entry = 1 where OrderId = ?";
        try {
            run.update(sql, orderId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}
