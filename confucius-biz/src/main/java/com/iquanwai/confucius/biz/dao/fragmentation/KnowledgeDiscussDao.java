package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.KnowledgeDiscuss;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by 三十文
 */
@Repository
public class KnowledgeDiscussDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<KnowledgeDiscuss> loadByKnowledgeId(Integer knowledgeId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM KnowledgeDiscuss WHERE KnowledgeId = ? AND Del = 0";
        ResultSetHandler<List<KnowledgeDiscuss>> h = new BeanListHandler<KnowledgeDiscuss>(KnowledgeDiscuss.class);
        try {
            return runner.query(sql, h, knowledgeId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public int voteKnowledgeDiscuss(Integer discussId, Boolean priority) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE KnowledgeDiscuss SET Priority = ? WHERE Id = ?";
        try {
            return runner.update(sql, priority, discussId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

}
