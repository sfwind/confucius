package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
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
        String sql = "INSERT INTO RiseClassMember (ClassId, ClassName, GroupId, MemberId, ProfileId, Active) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    riseClassMember.getClassId(),
                    riseClassMember.getClassName(),
                    riseClassMember.getGroupId(),
                    riseClassMember.getMemberId(),
                    riseClassMember.getProfileId(),
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

}