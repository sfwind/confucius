package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.domain.asst.UnderCommentCount;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.util.page.Page;
import org.apache.commons.collections.CollectionUtils;
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
 * Created by nethunder on 2017/1/13.
 */
@Repository
public class ApplicationSubmitDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(ApplicationSubmit applicationSubmit) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into ApplicationSubmit(ProfileId, Openid, ApplicationId, PlanId, ProblemId) " +
                "values(?,?,?,?,?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    applicationSubmit.getProfileId(),
                    applicationSubmit.getOpenid(), applicationSubmit.getApplicationId(),
                    applicationSubmit.getPlanId(), applicationSubmit.getProblemId());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 查询用户提交记录
     * @param applicationId 应用练习id
     * @param planId 计划id
     * @param openid openid
     */
    public ApplicationSubmit load(Integer applicationId, Integer planId, String openid) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ApplicationSubmit> h = new BeanHandler<>(ApplicationSubmit.class);
        String sql = "SELECT * FROM ApplicationSubmit where Openid=? and ApplicationId=? and PlanId=?";
        try {
            return run.query(sql, h, openid, applicationId, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<ApplicationSubmit> load(Integer applicationId, String openid) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);
        String sql = "SELECT * FROM ApplicationSubmit where Openid=? and ApplicationId=?";
        try {
            return run.query(sql, h, openid, applicationId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public List<ApplicationSubmit> load(Integer applicationId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);
        // TODO: 写死了大小
        String sql = "SELECT * FROM ApplicationSubmit where ApplicationId=? and Length>=15 order by UpdateTime desc limit 50";
        try {
            return run.query(sql, h, applicationId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public boolean firstAnswer(Integer id, String content, int length) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Content=?, Length=?, PublishTime = CURRENT_TIMESTAMP, LastModifiedTime = CURRENT_TIMESTAMP where Id=?";
        try {
            runner.update(sql, content, length, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public boolean answer(Integer id, String content, int length) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Content=?, Length=?, LastModifiedTime = CURRENT_TIMESTAMP where Id=?";
        try {
            runner.update(sql, content, length, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public boolean updatePointStatus(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set PointStatus=1 where Id=?";
        try {

            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public List<ApplicationSubmit> getPracticeSubmit(Integer practiceId, Page page) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from ApplicationSubmit where ApplicationId=? and Content is not null order by UpdateTime desc limit "
                + page.getOffset() + "," + page.getLimit();
        ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);
        try {
            return runner.query(sql, h, practiceId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<ApplicationSubmit> loadRequestCommentApplications(Integer problemId, int size) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from ApplicationSubmit where ProblemId =? and Content is not null " +
                "and Feedback = 0 and RequestFeedback =1 " +
                "order by length desc limit " + size;
        ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);
        try {
            return runner.query(sql, h, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<ApplicationSubmit> getHighlightSubmit(Integer practiceId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from ApplicationSubmit where ApplicationId=? and Priority=1 ";
        ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);
        try {
            return runner.query(sql, h, practiceId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public void highlight(Integer submitId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Priority=1, HighlightTime = now() where Id=?";
        try {

            runner.update(sql, submitId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void unHighlight(Integer submitId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Priority=0 where Id=?";
        try {

            runner.update(sql, submitId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void asstFeedback(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Feedback=1 where Id=?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<ApplicationSubmit> loadUnderCommentApplicationsIncludeSomeone(Integer problemId, int size, Date date,
                                                                              List<Integer> profileIds) {
        if (CollectionUtils.isEmpty(profileIds)) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String questionMark = produceQuestionMark(profileIds.size());
        ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);
        String sql = "select * from ApplicationSubmit where ProblemId =? " +
                "and Feedback=0 and RequestFeedback =0 and AddTime>? and ProfileId in (" + questionMark + ") " +
                "order by length desc limit " + size;
        List<Object> param = Lists.newArrayList();
        param.add(problemId);
        param.add(date);
        param.addAll(profileIds);

        try {
            return runner.query(sql, h, param.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<ApplicationSubmit> loadUnderCommentApplicationsExcludeSomeone(Integer problemId, int size, Date date,
                                                                              List<Integer> profileIds) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String questionMark = produceQuestionMark(profileIds.size());
        if (profileIds.size() != 0) {
            String sql = "select * from ApplicationSubmit where ProblemId =? " +
                    "and Feedback=0 and RequestFeedback =0 and AddTime>? and ProfileId not in (" + questionMark + ") " +
                    "order by length desc limit " + size;
            ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);

            List<Object> param = Lists.newArrayList();
            param.add(problemId);
            param.add(date);
            param.addAll(profileIds);

            try {
                return runner.query(sql, h, param.toArray());
            } catch (SQLException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        } else {
            String sql = "select * from ApplicationSubmit where ProblemId =? " +
                    "and Feedback=0 and RequestFeedback =0 and AddTime>? " +
                    "order by length desc limit " + size;
            ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);

            List<Object> param = Lists.newArrayList();
            param.add(problemId);
            param.add(date);

            try {
                return runner.query(sql, h, param.toArray());
            } catch (SQLException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }

        return Lists.newArrayList();
    }

    public void requestComment(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set RequestFeedback=1 where Id=?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<UnderCommentCount> getUnderCommentCount() {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<UnderCommentCount>> h = new BeanListHandler<>(UnderCommentCount.class);
        String sql = "select ProblemId,count(*) as count from ApplicationSubmit where RequestFeedback=1 and Feedback=0 group by ProblemId";
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public void updateContent(Integer id, String content) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Content=? where Id=?";
        try {
            runner.update(sql, content, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<ApplicationSubmit> loadSubmits(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        String questionMark = produceQuestionMark(ids.size());
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);
        String sql = "select * from ApplicationSubmit where id in (" + questionMark + ")";
        try {
            return runner.query(sql, h, ids.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<ApplicationSubmit> loadSubmitsByOpenIds(Integer problemId, List<Integer> profileIds) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ApplicationSubmit where ProblemId = ? and ProfileId in (" + produceQuestionMark(profileIds.size()) + ")";
        ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);
        List<Object> objects = Lists.newArrayList();
        objects.add(problemId);
        objects.addAll(profileIds);
        try {
            return runner.query(sql, h, objects.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
