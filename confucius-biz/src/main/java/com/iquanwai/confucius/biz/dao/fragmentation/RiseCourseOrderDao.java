package com.iquanwai.confucius.biz.dao.fragmentation;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.RiseCourseOrder;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/7/14.
 */
@Repository
public class RiseCourseOrderDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public int insert(RiseCourseOrder riseCourseOrder) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into RiseCourseOrder(ProfileId, Openid, ProblemId, OrderId) " +
                " VALUES (?, ?, ?, ?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    riseCourseOrder.getProfileId(), riseCourseOrder.getOpenid(), riseCourseOrder.getProblemId(), riseCourseOrder.getOrderId());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public RiseCourseOrder loadOrder(String orderId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<RiseCourseOrder> h = new BeanHandler<>(RiseCourseOrder.class);

        try {
            RiseCourseOrder order = run.query("SELECT * FROM RiseCourseOrder where OrderId=? ", h, orderId);
            return order;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public void entry(String orderId) {
        QueryRunner run = new QueryRunner(getDataSource());
        String sql = "Update RiseCourseOrder SET Entry = 1 where OrderId = ?";
        try {
            run.update(sql, orderId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}
