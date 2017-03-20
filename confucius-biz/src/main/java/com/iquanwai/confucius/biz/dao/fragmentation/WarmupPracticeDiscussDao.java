package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPracticeDiscuss;
import com.iquanwai.confucius.biz.util.DateUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by justin on 17/2/8.
 */
@Repository
public class WarmupPracticeDiscussDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(WarmupPracticeDiscuss discuss){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into WarmupPracticeDiscuss(WarmupPracticeId, Openid, RepliedId, Comment, " +
                "Priority, Del, RepliedOpenid, RepliedComment) " +
                "values(?,?,?,?,?,?,?,?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    discuss.getWarmupPracticeId(), discuss.getOpenid(), discuss.getRepliedId(),
                    discuss.getComment(), discuss.getPriority(), discuss.getDel(),
                    discuss.getRepliedOpenid(), discuss.getRepliedComment());

            return result.intValue();
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }

    public List<WarmupPracticeDiscuss> loadDiscuss(Integer practiceId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<WarmupPracticeDiscuss>> h = new BeanListHandler(WarmupPracticeDiscuss.class);
        String sql = "SELECT * FROM WarmupPracticeDiscuss where WarmupPracticeId = ? and Del = 0 " +
                "order by Priority desc, AddTime desc";
        try {
            return run.query(sql, h, practiceId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }


    public List<Integer> loadHotWarmupPracticeDiscussLastNDay(int day){
        QueryRunner run = new QueryRunner(getDataSource());
        //上限100个问题
        int limit = 100;
        Date date = DateUtils.beforeDays(new Date(), day);
        ResultSetHandler<List<Integer>> h = new BeanListHandler(Integer.class);
        String sql = "SELECT WarmupPracticeId FROM WarmupPracticeDiscuss where AddTime > ? group by WarmupPracticeId " +
                "order by Count(*) desc limit "+ limit;
        try {
            return run.query(sql, h, date);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public void highlight(int id){
        QueryRunner run = new QueryRunner(getDataSource());
        String sql = "Update WarmupPracticeDiscuss set Priority=1 where Id = ?";
        try {
            run.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }


}
