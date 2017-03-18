package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPractice;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 16/12/4.
 */
@Repository
public class WarmupPracticeDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());


    public List<WarmupPractice> loadPractices(List<Integer> practiceIds){
        QueryRunner run = new QueryRunner(getDataSource());
        String questionMark = produceQuestionMark(practiceIds.size());
        ResultSetHandler<List<WarmupPractice>> h = new BeanListHandler(WarmupPractice.class);
        String sql = "SELECT * FROM WarmupPractice where Id in ("+questionMark+")";
        try {
            return run.query(sql, h, practiceIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }
}
