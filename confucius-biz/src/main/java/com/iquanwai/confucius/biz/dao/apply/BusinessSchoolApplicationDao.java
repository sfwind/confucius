package com.iquanwai.confucius.biz.dao.apply;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.common.customer.BusinessSchoolApplication;
import com.iquanwai.confucius.biz.util.page.Page;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2017/9/27.
 */
@Repository
public class BusinessSchoolApplicationDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public BusinessSchoolApplication loadLastApproveApplication(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM BusinessSchoolApplication WHERE ProfileId = ? AND Del = 0 AND Status = 1 AND Valid = 1 Order by Id desc";
        try {
            return runner.query(sql, new BeanHandler<>(BusinessSchoolApplication.class), profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<BusinessSchoolApplication> loadList(Page page) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM BusinessSchoolApplication WHERE Status = 0 AND Del =0 AND Valid = 1 LIMIT " + page.getOffset() + "," + page.getLimit();
        try {
            return runner.query(sql, new BeanListHandler<>(BusinessSchoolApplication.class));
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer loadCount() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT count(*) from BusinessSchoolApplication WHERE Status = 0 AND Del =0 AND Valid = 1";
        try {
            return runner.query(sql, new ScalarHandler<Long>()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer reject(Integer id, String comment) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE BusinessSchoolApplication SET Status = 2,Comment = ?,CheckTime = CURRENT_TIMESTAMP WHERE Id = ?";
        try {
            return runner.update(sql, comment, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer approve(Integer id, Double coupon, String comment) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE BusinessSchoolApplication SET Status = 1,Coupon = ?,Comment = ?,CheckTime = CURRENT_TIMESTAMP WHERE Id = ?";
        try {
            return runner.update(sql, coupon, comment, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer ignore(Integer id, String comment) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE BusinessSchoolApplication SET Status = 3,Comment = ?,CheckTime = CURRENT_TIMESTAMP WHERE Id = ?";
        try {
            return runner.update(sql, comment, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public BusinessSchoolApplication loadCheckingApplication(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM BusinessSchoolApplication WHERE ProfileId = ? AND Del = 0 AND Status = 0 AND Valid = 1 Order by Id desc";
        try {
            return runner.query(sql, new BeanHandler<>(BusinessSchoolApplication.class), profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public BusinessSchoolApplication loadLatestInvalidApply(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from BusinessSchoolApplication where ProfileId = ? and Valid = 0 and Del = 0 order by Id desc limit 1";
        try {
            return runner.query(sql, new BeanHandler<>(BusinessSchoolApplication.class), profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public Integer validApply(String orderId, Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE BusinessSchoolApplication SET Valid = 1,OrderId = ? WHERE Id = ?";
        try {
            return runner.update(sql, orderId, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer assignInterviewer(Integer applyId, Integer interviewer) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE BusinessSchoolApplication SET Interviewer = ? WHERE Id = ?";
        try {
            return runner.update(sql, interviewer, applyId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
