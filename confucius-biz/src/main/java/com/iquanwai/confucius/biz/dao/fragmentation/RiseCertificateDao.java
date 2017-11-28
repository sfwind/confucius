package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.RiseCertificate;
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
 * Created by justin on 17/8/29.
 */
@Repository
public class RiseCertificateDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(RiseCertificate riseCertificate) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO RiseCertificate (ProfileId, Type, CertificateNo, Year, Month, GroupNo, ProblemName)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    riseCertificate.getProfileId(),
                    riseCertificate.getType(),
                    riseCertificate.getCertificateNo(),
                    riseCertificate.getYear(),
                    riseCertificate.getMonth(),
                    riseCertificate.getGroupNo(),
                    riseCertificate.getProblemName());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<RiseCertificate> loadRiseCertificatesByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseCertificate WHERE ProfileId = ? AND Del = 0";
        ResultSetHandler<List<RiseCertificate>> h = new BeanListHandler<>(RiseCertificate.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public RiseCertificate loadGraduateByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseCertificate where ProfileId = ? and Type in (1,2,3,5)";
        ResultSetHandler<RiseCertificate> h = new BeanHandler<>(RiseCertificate.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return null;
    }

}
