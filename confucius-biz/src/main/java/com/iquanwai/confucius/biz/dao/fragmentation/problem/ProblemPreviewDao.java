package com.iquanwai.confucius.biz.dao.fragmentation.problem;

import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemPreview;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class ProblemPreviewDao extends PracticeDBUtil {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    public ProblemPreview loadByScheduleId(Integer problemScheduleId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ProblemPreview WHERE ProblemScheduleId = ? AND DEL = 0";
        ResultSetHandler<ProblemPreview> h = new BeanHandler<ProblemPreview>(ProblemPreview.class);

        try {
           return runner.query(sql,h,problemScheduleId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return null;
    }

    public Integer insert(ProblemPreview problemPreview) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO ProblemPreview(Description,AudioId,VideoId,ProblemScheduleId,Updated) Values(?,?,?,?,?)";

        try {
            Long result = runner.insert(sql, new ScalarHandler<>(), problemPreview.getDescription(), problemPreview.getAudioId(),
                    problemPreview.getVideoId(), problemPreview.getProblemScheduleId(), problemPreview.getUpdated());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }


    public Integer update(ProblemPreview problemPreview) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = " Update ProblemPreview SET Description = ?,AudioId=?,VideoId=?,ProblemScheduleId=?,Updated=? WHERE ID = ?";
        try {
            return runner.update(sql, problemPreview.getDescription(), problemPreview.getAudioId(),
                    problemPreview.getVideoId(), problemPreview.getProblemScheduleId(),
                    problemPreview.getUpdated(), problemPreview.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }


}
