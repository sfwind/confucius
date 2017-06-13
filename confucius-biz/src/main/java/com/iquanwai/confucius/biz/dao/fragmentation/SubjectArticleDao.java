package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.domain.asst.UnderCommentCount;
import com.iquanwai.confucius.biz.po.fragmentation.SubjectArticle;
import com.iquanwai.confucius.biz.util.page.Page;
import org.apache.commons.collections.CollectionUtils;
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
 * Created by nethunder on 2017/3/8.
 */
@Repository
public class SubjectArticleDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(SubjectArticle subjectArticle) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into SubjectArticle(ProfileId, Openid, ProblemId, AuthorType, Sequence,Title, Content, Length) " +
                "values(?,?,?,?,?,?,?,?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    subjectArticle.getProfileId(), subjectArticle.getOpenid(), subjectArticle.getProblemId(),
                    subjectArticle.getAuthorType(), subjectArticle.getSequence(), subjectArticle.getTitle(),
                    subjectArticle.getContent(), subjectArticle.getLength());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public boolean update(SubjectArticle subjectArticle) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update SubjectArticle set Title = ?,Content = ?, Length = ? where Id = ?";
        try {
            runner.update(sql, subjectArticle.getTitle(), subjectArticle.getContent(),
                    subjectArticle.getLength(), subjectArticle.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public int count(Integer problemId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select count(1) from SubjectArticle where ProblemId = ?";
        try {
            Long id = runner.query(sql, new ScalarHandler<>(), problemId);
            return id.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<SubjectArticle> loadArticles(Integer problemId, Page page) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<SubjectArticle>> h = new BeanListHandler<>(SubjectArticle.class);
        String sql = "select * from SubjectArticle where ProblemId = ? order by Sequence desc,UpdateTime desc limit " + page.getOffset() + "," + page.getLimit();
        try {
            return runner.query(sql, h, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<SubjectArticle> loadArticles(Integer problemId, String openId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<SubjectArticle>> h = new BeanListHandler<>(SubjectArticle.class);
        String sql = "select * from SubjectArticle where ProblemId = ? and openid = ? order by Sequence desc,UpdateTime desc";
        try {
            return runner.query(sql, h, problemId, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<SubjectArticle> loadRequestCommentArticles(Integer problemId, int size) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<SubjectArticle>> h = new BeanListHandler<>(SubjectArticle.class);
        String sql = "select * from SubjectArticle where ProblemId = ? and Feedback=0 and AuthorType=1 " +
                "and RequestFeedback =1 " +
                "order by length desc limit " + size;
        try {
            return runner.query(sql, h, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<SubjectArticle> loadUnderCommentArticlesIncludeSomeone(Integer problemId, int size, Date date,
                                                                       List<Integer> profileIds) {
        if (CollectionUtils.isEmpty(profileIds)) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String questionMark = produceQuestionMark(profileIds.size());
        ResultSetHandler<List<SubjectArticle>> h = new BeanListHandler<>(SubjectArticle.class);
        String sql = "select * from SubjectArticle where ProblemId = ? and Feedback=0 and AuthorType=1 " +
                "and RequestFeedback =0 and AddTime>? and ProfileId in (" + questionMark + ") " +
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

    public List<SubjectArticle> loadUnderCommentArticlesExcludeSomeone(Integer problemId, int size, Date date,
                                                                       List<Integer> profileIds) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String questionMark = produceQuestionMark(profileIds.size());
        if (profileIds.size() != 0) {
            ResultSetHandler<List<SubjectArticle>> h = new BeanListHandler<>(SubjectArticle.class);
            String sql = "select * from SubjectArticle where ProblemId = ? and Feedback=0 and AuthorType=1 " +
                    "and RequestFeedback =0 and AddTime>? and ProfileId not in (" + questionMark + ") " +
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
        } else {
            ResultSetHandler<List<SubjectArticle>> h = new BeanListHandler<>(SubjectArticle.class);
            String sql = "select * from SubjectArticle where ProblemId = ? and Feedback=0 and AuthorType=1 " +
                    "and RequestFeedback =0 and AddTime>? " +
                    "order by length desc limit " + size;
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

    public void asstFeedback(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update SubjectArticle set Feedback=1 where Id=?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void requestComment(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update SubjectArticle set RequestFeedback=1 where Id=?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<UnderCommentCount> getUnderCommentCount() {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<UnderCommentCount>> h = new BeanListHandler<>(UnderCommentCount.class);
        String sql = "select ProblemId,count(*) as count from SubjectArticle where RequestFeedback=1 and Feedback=0 group by ProblemId";
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public void updateContent(Integer id, String content) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update SubjectArticle set Content=? where Id=?";
        try {
            runner.update(sql, content, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<SubjectArticle> loadArticles(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        String questionMark = produceQuestionMark(ids.size());
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<SubjectArticle>> h = new BeanListHandler<>(SubjectArticle.class);
        String sql = "select * from SubjectArticle where id in (" + questionMark + ")";
        try {
            return runner.query(sql, h, ids.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
