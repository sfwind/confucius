package com.iquanwai.confucius.biz.dao.apply;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.confucius.biz.util.page.Page;
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
 * Created by nethunder on 2017/9/27.
 */
@Repository
public class BusinessSchoolApplicationDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // TODO wait
    public BusinessSchoolApplication loadLastApproveApplication(Integer profileId, Integer memberTypeId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM BusinessSchoolApplication WHERE ProfileId = ? AND MemberTypeId = ? AND Del = 0 AND Status = 1 AND Valid = 1 Order by Id desc";
        try {
            return runner.query(sql, new BeanHandler<>(BusinessSchoolApplication.class), profileId, memberTypeId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    // TODO wait
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

    /**
     * 获得用户的有效申请
     *
     * @param profileId 用户id
     * @return 有效申请列表
     */
    public List<BusinessSchoolApplication> loadApplyList(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM BusinessSchoolApplication WHERE ProfileId = ? AND Del = 0 AND Valid = 1 Order by Id desc";
        try {
            return runner.query(sql, new BeanListHandler<>(BusinessSchoolApplication.class), profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    // TODO wait
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

    // TODO wait
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

    // TODO wait
    public BusinessSchoolApplication loadLatestInvalidApply(Integer profileId, Integer memberTypeId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from BusinessSchoolApplication where ProfileId = ? and Valid = 0 and Del = 0 and MemberTypeId = ? order by Id desc limit 1";
        try {
            return runner.query(sql, new BeanHandler<>(BusinessSchoolApplication.class), profileId, memberTypeId);
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
        String sql = "UPDATE BusinessSchoolApplication SET Interviewer = ?,AssignTime= now() WHERE Id = ?";
        try {
            return runner.update(sql, interviewer, applyId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 根据教练加载正在审核中的商学院申请
     *
     * @param interviewer
     * @return
     */
    // TODO wait
    public List<BusinessSchoolApplication> loadByInterviewer(Integer interviewer, Page page) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from BusinessSchoolApplication where interviewer = ? and status = 0 and del = 0 LIMIT " + page.getOffset() + "," + page.getLimit();
        ResultSetHandler<List<BusinessSchoolApplication>> h = new BeanListHandler<>(BusinessSchoolApplication.class);
        try {
            return runner.query(sql, h, interviewer);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    // TODO wait
    public Integer loadAssistBACount(Integer interviewer) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT count(*) from BusinessSchoolApplication WHERE interviewer = ? and status = 0 AND Del =0";
        try {
            return runner.query(sql, new ScalarHandler<Long>(), interviewer).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 获得最近一次被审批过的商学院申请
     *
     * @param profileId 用户id
     * @return 获得最新一次被审核的记录
     */
    public BusinessSchoolApplication getLastVerifiedByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = " select * from BusinessSchoolApplication where profileId = ? and status != 0 and del = 0 and Valid = 1  order by UpdateTime desc";

        try {
            return runner.query(sql, new BeanHandler<>(BusinessSchoolApplication.class), profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    /**
     * 插入申请信息
     *
     * @param businessSchoolApplication 申请记录
     * @return 主键id
     */
    public Integer insert(BusinessSchoolApplication businessSchoolApplication) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO BusinessSchoolApplication(SubmitId, ProfileId, Status, CheckTime, IsDuplicate, Deal, " +
                "OriginMemberType,SubmitTime,DealTime,Comment,LastVerified,Valid,MemberTypeId) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(), businessSchoolApplication.getSubmitId(), businessSchoolApplication.getProfileId(),
                    businessSchoolApplication.getStatus(), businessSchoolApplication.getCheckTime(), businessSchoolApplication.getIsDuplicate(),
                    businessSchoolApplication.getDeal(), businessSchoolApplication.getOriginMemberType(),
                    businessSchoolApplication.getSubmitTime(), businessSchoolApplication.getDealTime(),
                    businessSchoolApplication.getComment(), businessSchoolApplication.getLastVerified(),
                    businessSchoolApplication.getValid(), businessSchoolApplication.getMemberTypeId()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public Integer expiredApply(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE BusinessSchoolApplication SET Expired = 1 WHERE Id = ?";
        try {
            return runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer entryApply(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE BusinessSchoolApplication SET Entry = 1 WHERE Id = ?";
        try {
            return runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
