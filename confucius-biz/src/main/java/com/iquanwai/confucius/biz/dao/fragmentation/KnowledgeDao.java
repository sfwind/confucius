package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.Knowledge;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class KnowledgeDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public Integer insertKnowledge(Knowledge knowledge) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO Knowledge (" +
                "Knowledge, Step, Analysis, Means, Keynote, " +
                "AnalysisPic, MeansPic, KeynotePic, Pic, " +
                "AnalysisAudioId, MeansAudioId, KeynoteAudioId, AudioId,Updated " +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,2)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    knowledge.getKnowledge(), knowledge.getStep(), knowledge.getAnalysis(),
                    knowledge.getMeans(), knowledge.getKeynote(), knowledge.getAnalysisPic(),
                    knowledge.getMeansPic(), knowledge.getKeynotePic(), knowledge.getPic(),
                    knowledge.getAnalysisAudioId(), knowledge.getMeansAudioId(),
                    knowledge.getKeynoteAudioId(), knowledge.getAudioId());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer updateKnowledge(Knowledge knowledge) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE Knowledge SET Knowledge = ?, Step = ?, Analysis = ?, Means = ?, Keynote = ?," +
                "AnalysisAudioId=?, MeansAudioId=?, KeynoteAudioId=?, AudioId=?,Updated = ? WHERE Id = ?";
        try {
            return runner.update(sql, knowledge.getKnowledge(), knowledge.getStep(),
                    knowledge.getAnalysis(), knowledge.getMeans(), knowledge.getKeynote(),
                    knowledge.getAnalysisAudioId(), knowledge.getMeansAudioId(),
                    knowledge.getKeynoteAudioId(), knowledge.getAudioId(), knowledge.getUpdated(), knowledge.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 查询所有知识点
     */
    public List<Knowledge> queryAllKnowledges() {
        QueryRunner runner = new QueryRunner(getDataSource());

        ResultSetHandler<List<Knowledge>> h = new BeanListHandler<>(Knowledge.class);
        String sql = "SELECT * FROM Knowledge";
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<Knowledge> queryByKnowledgeIds(List<Integer> ids) {
        if (ids.size() == 0) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM Knowledge WHERE Id IN (" + produceQuestionMark(ids.size()) + ");";
        ResultSetHandler<List<Knowledge>> h = new BeanListHandler<>(Knowledge.class);
        try {
            return runner.query(sql, h, ids.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
