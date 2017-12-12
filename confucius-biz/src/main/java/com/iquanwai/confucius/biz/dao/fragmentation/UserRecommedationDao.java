package com.iquanwai.confucius.biz.dao.fragmentation;


import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;


@Repository
public class UserRecommedationDao extends PracticeDBUtil{

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 插入用户推荐表
     * @return
     */
    public int insert(Integer profileId,String openId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into UserRecommendation(profileId,FriendOpenId) values (?,?)";

        try {
           Long result = runner.insert(sql,new ScalarHandler<>(),profileId,openId);
           return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return -1;
    }
}
