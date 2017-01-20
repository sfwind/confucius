package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.Comment;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by nethunder on 2017/1/20.
 */
@Repository
public class CommentDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public int insert(Comment comment) {
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);
        String insertSql = "insert into Comment(Type, ReferencedId, CommentOpenId, Content) " +
                "VALUES (?,?,?,?)";
        try {
            Future<Integer> result = asyncRun.update(insertSql,
                    comment.getType(), comment.getReferencedId(), comment.getCommentOpenId(), comment.getContent());
            return result.get();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            // ignore
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
        }

        return -1;
    }

    public List<Comment> loadComments(Integer type, Integer referId, Integer page) {
        if(page<1){
            page = 1;
        }
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Comment>> h = new BeanListHandler<Comment>(Comment.class);
        String sql = "SELECT * FROM Comment where Type = ? and ReferencedId = ? order by AddTime desc limit " + (page-1)*5 + ",5";
        try {
            return run.query(sql, h, type, referId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
