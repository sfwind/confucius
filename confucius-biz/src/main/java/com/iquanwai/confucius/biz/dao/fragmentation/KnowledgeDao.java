package com.iquanwai.confucius.biz.dao.fragmentation;

import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.Knowledge;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class KnowledgeDao extends PracticeDBUtil {

    Logger logger = LoggerFactory.getLogger(getClass());

    public Integer insertKnowledge(Knowledge knowledge) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO Knowledge (" +
                "Knowledge, Step, Analysis, Means, Keynote, " +
                "AnalysisPic, MeansPic, KeynotePic, Pic, Audio, " +
                "AnalysisAudio, MeansAudio, KeynoteAudio" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    knowledge.getKnowledge(), knowledge.getStep(), knowledge.getAnalysis(),
                    knowledge.getMeans(), knowledge.getKeynote(), knowledge.getAnalysisPic(),
                    knowledge.getMeansPic(), knowledge.getKeynotePic(), knowledge.getPic(),
                    knowledge.getAudio(), knowledge.getAnalysisAudio(), knowledge.getMeansAudio(),
                    knowledge.getKeynoteAudio());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

}
