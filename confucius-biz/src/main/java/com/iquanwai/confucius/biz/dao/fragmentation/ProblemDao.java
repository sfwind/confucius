package com.iquanwai.confucius.biz.dao.fragmentation;

import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class ProblemDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public Problem loadProblem(Integer problemId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM Problem WHERE Id = ?";
        ResultSetHandler<Problem> h = new BeanHandler<>(Problem.class);
        try {
            return runner.query(sql, h, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public int saveProblem(Problem problem) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO Problem(Problem, Length, CatalogId, SubCatalogId, " +
                "Author, AuthorPic, DifficultyScore, UsefulScore, DescPic, " +
                "AudioId, Who, How, Why, Trial, Abbreviation) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(), problem.getProblem(), problem.getLength(),
                    problem.getCatalogId(), problem.getSubCatalogId(), problem.getAuthor(),
                    problem.getAuthorPic(), problem.getDifficultyScore(), problem.getUsefulScore(),
                    problem.getDescPic(), problem.getAudioId(), problem.getWho(),
                    problem.getHow(), problem.getWhy(), problem.getTrial(), problem.getAbbreviation());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public void updateProblem(Problem problem) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update Problem set Problem =?, Length=?, CatalogId=?, SubCatalogId=?, " +
                "Author=?, AuthorPic=?, DifficultyScore=?, UsefulScore=?, DescPic=?, " +
                "AudioId=?, Who=?, How=?, Why=?, Trial=?, Abbreviation=? where id=?";
        try {
            runner.update(sql, problem.getProblem(), problem.getLength(),
                    problem.getCatalogId(), problem.getSubCatalogId(), problem.getAuthor(),
                    problem.getAuthorPic(), problem.getDifficultyScore(), problem.getUsefulScore(),
                    problem.getDescPic(), problem.getAudioId(), problem.getWho(), problem.getHow(),
                    problem.getWhy(), problem.getTrial(), problem.getAbbreviation(), problem.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}
