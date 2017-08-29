package com.iquanwai.confucius.biz.dao.fragmentation;

import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class RiseClassMemberDao extends PracticeDBUtil {

    Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(RiseClassMember riseClassMember) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO RiseClassMember (ClassId, GroupId, MemberId, ProfileId, Active) " +
                "VALUES (?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    riseClassMember.getClassId(),
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

}
