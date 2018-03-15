package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPractice;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPracticeDiscuss;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.page.Page;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
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


    public List<WarmupPracticeDiscuss> loadDiscuss(Integer practiceId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<WarmupPracticeDiscuss>> h = new BeanListHandler<>(WarmupPracticeDiscuss.class);
        String sql = "SELECT * FROM WarmupPracticeDiscuss where WarmupPracticeId = ? and Del = 0 " +
                "order by Priority desc, AddTime desc";
        try {
            return run.query(sql, h, practiceId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }


    public List<WarmupPracticeDiscuss> loadTargetDiscuss(Integer practiceId, String currentDate) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<WarmupPracticeDiscuss>> h = new BeanListHandler<>(WarmupPracticeDiscuss.class);
        String sql = "SELECT * FROM WarmupPracticeDiscuss WHERE WarmupPracticeId = ? AND AddTime LIKE ? AND  DEL = 0";

        try {
            return runner.query(sql, h, practiceId, "%" + currentDate + "%");
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<Integer> loadHotWarmupPracticeDiscussLastNDay(int day, Page page) {
        QueryRunner run = new QueryRunner(getDataSource());
        Date date = DateUtils.beforeDays(new Date(), day);
        ResultSetHandler<List<Integer>> h = new ColumnListHandler<>("WarmupPracticeId");
        String sql = "SELECT WarmupPracticeId FROM WarmupPracticeDiscuss where AddTime > ? group by WarmupPracticeId " +
                "order by Count(*) desc limit " + page.getOffset() + "," + page.getLimit();
        try {
            return run.query(sql, h, date);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<WarmupPracticeDiscuss> loadCurrentDayDiscuss(String currentDate) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<WarmupPracticeDiscuss>> h = new BeanListHandler<>(WarmupPracticeDiscuss.class);
        String sql = "SELECT * from WarmupPracticeDiscuss WHERE AddTime like ? AND DEL = 0 ";

        try {
            return runner.query(sql, h,currentDate+"%");
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<WarmupPracticeDiscuss> loadCurrentDayDiscussByWarmUp(String currentDate, WarmupPractice warmupPractice) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<WarmupPracticeDiscuss>> h = new BeanListHandler<>(WarmupPracticeDiscuss.class);
        String sql = "SELECT * from WarmupPracticeDiscuss WHERE WarmupPracticeId = ? AND  AddTime like ? AND DEL = 0 ";

        try {
            return runner.query(sql, h,warmupPractice.getId(),currentDate+"%");
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<WarmupPracticeDiscuss> loadByRelays(List<Integer> replays) {
        if (replays.size() == 0) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String questionMark = produceQuestionMark(replays.size());
        ResultSetHandler<List<WarmupPracticeDiscuss>> h = new BeanListHandler<>(WarmupPracticeDiscuss.class);
        String sql = "SELECT * FROM WarmupPracticeDiscuss WHERE RepliedId in (" + questionMark + " ) AND DEL = 0";

        try {
            return runner.query(sql, h, replays.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }


    public int insert(WarmupPracticeDiscuss discuss) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into WarmupPracticeDiscuss(WarmupPracticeId, Profileid, RepliedId, Comment, " +
                "Priority, Del, RepliedProfileid, RepliedComment, OriginDiscussId) " +
                "values(?,?,?,?,?,?,?,?,?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    discuss.getWarmupPracticeId(), discuss.getProfileId(),
                    discuss.getRepliedId(), discuss.getComment(), discuss.getPriority(), discuss.getDel(),
                    discuss.getRepliedProfileId(), discuss.getRepliedComment(), discuss.getOriginDiscussId());

            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }

    public void highlight(int id) {
        QueryRunner run = new QueryRunner(getDataSource());
        String sql = "Update WarmupPracticeDiscuss set Priority=1 where Id = ?";
        try {
            run.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public int deleteDiscussById(int id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update WarmupPracticeDiscuss set Del = 1 where Id = ?";
        try {
            return runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

}
