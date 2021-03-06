package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.domain.fragmentation.ClassMember;
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
 * Created by 三十文
 */
@Repository
public class FragmentClassMemberDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public Integer insert(ClassMember classMember) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO ClassMember (ProfileId, ClassName, GroupId, MemberTypeId) VALUES (?, ?, ?, ?)";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(),
                    classMember.getProfileId(),
                    classMember.getClassName(),
                    classMember.getGroupId(),
                    classMember.getMemberTypeId()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<ClassMember> loadByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ClassMember WHERE ProfileId = ? AND Del = 0";
        ResultSetHandler<List<ClassMember>> h = new BeanListHandler<>(ClassMember.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<ClassMember> loadByProfileIdAndMemberTypeId(Integer profileId, Integer memberTypeId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ClassMember WHERE ProfileId = ? AND MemberTypeId = ? AND Del = 0";
        ResultSetHandler<List<ClassMember>> h = new BeanListHandler<>(ClassMember.class);
        try {
            return runner.query(sql, h, profileId, memberTypeId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<ClassMember> loadByProfileIds(List<Integer> profileIds) {
        if (profileIds.size() == 0) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ClassMember WHERE ProfileId in (" + produceQuestionMark(profileIds.size()) + ") AND Del = 0";
        ResultSetHandler<List<ClassMember>> h = new BeanListHandler<>(ClassMember.class);
        try {
            return runner.query(sql, h, profileIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return Lists.newArrayList();
    }

    public List<ClassMember> loadActiveByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ClassMember WHERE ProfileId = ? AND Active = 1 AND Del = 0";
        ResultSetHandler<List<ClassMember>> h = new BeanListHandler<>(ClassMember.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public ClassMember loadLatestByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ClassMember WHERE ProfileId = ? AND Del = 0 ORDER BY AddTime DESC";
        ResultSetHandler<ClassMember> h = new BeanHandler<>(ClassMember.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
