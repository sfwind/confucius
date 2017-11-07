package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampConfig;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
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

@Repository
public class RiseClassMemberDao extends PracticeDBUtil {

    Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(RiseClassMember riseClassMember) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO RiseClassMember (ClassName, GroupId, MemberId, ProfileId, Year, Month, Active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    riseClassMember.getClassName(),
                    riseClassMember.getGroupId(),
                    riseClassMember.getMemberId(),
                    riseClassMember.getProfileId(),
                    riseClassMember.getYear(),
                    riseClassMember.getMonth(),
                    riseClassMember.getActive());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public int update(RiseClassMember riseClassMember) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE RiseClassMember SET ClassName = ?, GroupId = ?, Active = ? WHERE Id = ?";
        try {
            return runner.update(sql,
                    riseClassMember.getClassName(),
                    riseClassMember.getGroupId(),
                    riseClassMember.getActive(),
                    riseClassMember.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<RiseClassMember> queryByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE ProfileId = ? AND Del = 0";
        ResultSetHandler<List<RiseClassMember>> h = new BeanListHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public RiseClassMember queryByMemberId(String memberId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE MemberId = ? AND Active = 1 AND Del = 0";
        ResultSetHandler<RiseClassMember> h = new BeanHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, memberId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public RiseClassMember queryValidClassMemberByMemberId(String memberId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE MemberId = ? AND Del = 0 LIMIT 1";
        ResultSetHandler<RiseClassMember> h = new BeanHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, memberId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * 单笔查询
     * @param profileId 用户 id
     * @param year 年份
     * @param month 月份
     */
    public RiseClassMember queryByProfileIdAndTime(Integer profileId, Integer year, Integer month) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE ProfileId = ? AND Year = ? AND Month = ? AND Del = 0";
        ResultSetHandler<RiseClassMember> h = new BeanHandler<RiseClassMember>(RiseClassMember.class);
        try {
            return runner.query(sql, h, profileId, year, month);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * 获取对应年月下的所有有效数据
     */
    public List<RiseClassMember> loadAllByYearMonth(Integer year, Integer month) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE Year = ? AND Month = ? AND Del = 0";
        ResultSetHandler<List<RiseClassMember>> h = new BeanListHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, year, month);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<RiseClassMember> loadActiveRiseClassMembers() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE Active = 1 AND Del = 0";
        ResultSetHandler<List<RiseClassMember>> h = new BeanListHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public int del(Integer riseClassMemberId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE RiseClassMember SET Del = 1 WHERE Id = ?";
        try {
            return runner.update(sql, riseClassMemberId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public int batchUpdateGroupId(List<Integer> ids, String groupId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE RiseClassMember SET GroupId = ? WHERE Id IN (" + produceQuestionMark(ids.size()) + ") AND Del = 0";
        List<Object> objects = Lists.newArrayList();
        objects.add(groupId);
        objects.addAll(ids);
        try {
            return runner.update(sql, objects.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 将对应年月下的有效人员的 Active 字段置为 1
     */
    public int batchUpdateActive(Integer year, Integer month, Integer active) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE RiseClassMember SET Active = ? WHERE Year = ? AND Month = ? AND Del = 0";
        try {
            return runner.update(sql, active, year, month);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<RiseClassMember> batchQueryByProfileIds(List<Integer> profileIds) {
        if (profileIds.size() == 0) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE ProfileId in (" + produceQuestionMark(profileIds.size()) + ") AND Del = 0";
        ResultSetHandler<List<RiseClassMember>> h = new BeanListHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, profileIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<RiseClassMember> loadByClassName(String className) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE ClassName = ? AND (GroupId IS NOT NULL AND GroupId != '') AND Del = 0 ORDER BY ClassName, GroupId";
        ResultSetHandler<List<RiseClassMember>> h = new BeanListHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, className);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    /**
     * 近似查询 MemberId 获取当前学号中最近的一个人
     */
    public RiseClassMember loadLatestLikeMemberIdRiseClassMember(String likeMemberId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE MemberId LIKE '" + likeMemberId + "%' AND Del = 0 ORDER BY Id DESC Limit 1";
        ResultSetHandler<RiseClassMember> h = new BeanHandler<RiseClassMember>(RiseClassMember.class);
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public RiseClassMember loadPurchaseRiseClassMember(Integer profileId, String className, MonthlyCampConfig monthlyCampConfig) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE ClassName = ? AND ProfileId = ? AND YEAR = ? AND Month = ? AND Active = 0 AND Del = 0";
        ResultSetHandler<RiseClassMember> h = new BeanHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h,
                    className,
                    profileId,
                    monthlyCampConfig.getSellingYear(),
                    monthlyCampConfig.getSellingMonth());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<RiseClassMember> loadUnGroupMember() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE (GroupId IS NULL OR GroupId = '') AND Del = 0 ORDER BY ClassName";
        ResultSetHandler<List<RiseClassMember>> h = new BeanListHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<RiseClassMember> loadUnGroupMemberPage(Page page) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE (GroupId IS NULL OR GroupId = '') AND Del = 0  ORDER BY ClassName LIMIT "
                + page.getOffset() + ", " + page.getPageSize();
        ResultSetHandler<List<RiseClassMember>> h = new BeanListHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
