package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.SubjectArticle;
import com.iquanwai.confucius.biz.util.page.Page;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2017/3/8.
 */
@Repository
public class SubjectArticleDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(SubjectArticle subjectArticle){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into SubjectArticle(Openid, ProblemId, AuthorType, Sequence,Title, Content) " +
                "values(?,?,?,?,?,?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    subjectArticle.getOpenid(), subjectArticle.getProblemId(),
                    subjectArticle.getAuthorType(), subjectArticle.getSequence(), subjectArticle.getTitle(),
                    subjectArticle.getContent());
            return insertRs.intValue();
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public boolean update(SubjectArticle subjectArticle) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update SubjectArticle set Title = ?,Content = ? where Id = ?";
        try{
            runner.update(sql, subjectArticle.getTitle(), subjectArticle.getContent(), subjectArticle.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public int count(Integer problemId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select count(1) from SubjectArticle where ProblemId = ?";
        try{
            return runner.query(sql, new ScalarHandler<Long>(), problemId).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<SubjectArticle> loadArticles(Integer problemId){
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<SubjectArticle>> h = new BeanListHandler<SubjectArticle>(SubjectArticle.class);
        String sql = "select * from SubjectArticle where ProblemId = ? order by Sequence desc,UpdateTime desc";
        try{
            return runner.query(sql,h,problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<SubjectArticle> loadArticles(Integer problemId,Page page){
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<SubjectArticle>> h = new BeanListHandler<SubjectArticle>(SubjectArticle.class);
        String sql = "select * from SubjectArticle where ProblemId = ? order by Sequence desc,UpdateTime desc limit " + page.getOffset() + "," + page.getLimit();
        try{
            return runner.query(sql,h,problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<SubjectArticle> loadArticles(Integer problemId,String openId){
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<SubjectArticle>> h = new BeanListHandler<SubjectArticle>(SubjectArticle.class);
        String sql = "select * from SubjectArticle where ProblemId = ? and openid = ? order by Sequence desc,UpdateTime desc";
        try{
            return runner.query(sql,h,problemId,openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
