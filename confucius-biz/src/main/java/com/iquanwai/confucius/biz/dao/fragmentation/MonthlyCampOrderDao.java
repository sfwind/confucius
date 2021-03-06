package com.iquanwai.confucius.biz.dao.fragmentation;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampOrder;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class MonthlyCampOrderDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(MonthlyCampOrder monthlyCampOrder) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO MonthlyCampOrder (OrderId, ProfileId, Year, Month) " +
                "VALUES (?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    monthlyCampOrder.getOrderId(),
                    monthlyCampOrder.getProfileId(), monthlyCampOrder.getYear(), monthlyCampOrder.getMonth());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public MonthlyCampOrder loadCampOrder(String orderId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM MonthlyCampOrder WHERE OrderId = ?";
        ResultSetHandler<MonthlyCampOrder> h = new BeanHandler<>(MonthlyCampOrder.class);
        try {
            return runner.query(sql, h, orderId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public void entry(String orderId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update MonthlyCampOrder SET Entry = 1 WHERE OrderId = ?";
        try {
            runner.update(sql, orderId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}
