package com.iquanwai.confucius.biz.dao.fragmentation;

import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.RiseCertificate;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 17/8/29.
 */
@Repository
public class RiseCertificateDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

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
