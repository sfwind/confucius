package com.iquanwai.confucius.biz.dao.course;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.systematism.ClassMember;
import com.iquanwai.confucius.biz.util.DateUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by justin on 16/8/29.
 */
@Repository
public class ClassMemberDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public ClassMember classMember(Integer profileId, Integer courseId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ClassMember> h = new BeanHandler<>(ClassMember.class);
        try {
            ClassMember classMember = run.query("SELECT * FROM ClassMember where ProfileId=? and CourseId=? and Graduate=0",
                    h, profileId, courseId);
            return classMember;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public List<ClassMember> classMember(Integer profileId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ClassMember>> h = new BeanListHandler<>(ClassMember.class);

        try {
            List<ClassMember> classMember = run.query("SELECT * FROM ClassMember where ProfileId=? and Graduate = 0",
                    h, profileId);
            return classMember;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    /**
     * 查询还有N天要关闭课程的学员
     *
     * @param date
     */
    public List<ClassMember> willCloseMembers(Date date) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ClassMember>> h = new BeanListHandler<>(ClassMember.class);

        try {
            List<ClassMember> classMember = run.query("SELECT * FROM ClassMember where CloseDate = ?", h, DateUtils.parseDateToString(date));
            return classMember;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public List<ClassMember> graduateInfo(Integer profileId, Integer courseId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ClassMember>> h = new BeanListHandler<>(ClassMember.class);

        try {
            List<ClassMember> classMember = run.query("SELECT * FROM ClassMember where ProfileId=? and CourseId=? and Graduate = 1 " +
                    "order by UpdateTime desc", h, profileId, courseId);
            return classMember;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }


    public void progress(Integer profileId, Integer classId, String progress) {
        QueryRunner run = new QueryRunner(getDataSource());

        try {
            run.update("UPDATE ClassMember SET Progress =? " +
                    "where ProfileId=? and ClassId=?", progress, profileId, classId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void complete(Integer profileId, Integer classId, String complete) {
        QueryRunner run = new QueryRunner(getDataSource());

        try {
            run.update("UPDATE ClassMember SET Complete =? " +
                    "where ProfileId=? and ClassId=?", complete, profileId, classId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public ClassMember getClassMember(Integer classId, Integer profileId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ClassMember> h = new BeanHandler<>(ClassMember.class);

        try {
            ClassMember classMember = run.query("SELECT * FROM ClassMember where ProfileId=? and ClassId=? ",
                    h, profileId, classId);
            return classMember;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public List<ClassMember> getClassMember(Integer classId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ClassMember>> h = new BeanListHandler<>(ClassMember.class);

        try {
            List<ClassMember> classMember = run.query("SELECT * FROM ClassMember where ClassId=? ",
                    h, classId);
            return classMember;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public List<ClassMember> getClassMember(Integer classId, Date closeDate) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ClassMember>> h = new BeanListHandler<>(ClassMember.class);

        try {
            List<ClassMember> classMember = run.query("SELECT * FROM ClassMember where ClassId=? and CloseDate > ?",
                    h, classId, closeDate);
            return classMember;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }


    public void entry(ClassMember classMember) {
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "INSERT INTO ClassMember(ClassId, CourseId, ProfileId, MemberId, Graduate, CloseDate) " +
                "VALUES(?, ?, ?, ?, 0, ?)";
        try {
            run.insert(insertSql, new ScalarHandler<>(),
                    classMember.getClassId(), classMember.getCourseId(),
                    classMember.getProfileId(),
                    classMember.getMemberId(), classMember.getCloseDate());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public Integer classMemberNumber(Integer classId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<Long>();

        try {
            Long number = run.query("SELECT count(*) FROM ClassMember where ClassId=?", h, classId);
            return number.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }

    public void graduate(Integer id) {
        QueryRunner run = new QueryRunner(getDataSource());

        try {
            run.update("UPDATE ClassMember SET Graduate =1 " +
                    "where id=?", id);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateCertificateNo(Integer classId, Integer profileId, String certificateNo) {
        QueryRunner run = new QueryRunner(getDataSource());

        try {
            run.update("UPDATE ClassMember SET CertificateNo=? " +
                    "where ProfileId=? and ClassId=? ", certificateNo, profileId, classId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public ClassMember loadByCertificateNo(String certificateNo) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ClassMember> h = new BeanHandler<>(ClassMember.class);
        try {
            ClassMember classMember = run.query("SELECT * FROM ClassMember where CertificateNo=?",
                    h, certificateNo);
            return classMember;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public void reEntry(Integer classMemberId, Date closeDate) {
        QueryRunner run = new QueryRunner(getDataSource());

        try {
            run.update("UPDATE ClassMember SET Graduate =0, closeDate=? " +
                    "where Id=?", closeDate, classMemberId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}
