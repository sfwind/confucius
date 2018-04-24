package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationPractice;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2017/1/13.
 */
@Repository
public class ApplicationPracticeDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<ApplicationPractice> getPracticeByProblemId(Integer problemId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ApplicationPractice>> h = new BeanListHandler<>(ApplicationPractice.class);
        String sql = "SELECT * FROM ApplicationPractice where ProblemId=? and Del=0";
        try {
            return run.query(sql, h, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<ApplicationPractice> getAllPracticeByProblemId(Integer problemId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ApplicationPractice>> h = new BeanListHandler<>(ApplicationPractice.class);
        String sql = "SELECT * FROM ApplicationPractice where ProblemId=?";
        try {
            return run.query(sql, h, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer updateApplicationPracticeById(Integer id, String topic, String description,int difficulty) {
        QueryRunner runner = new QueryRunner(getDataSource());

        String sql;
        if(isOriginUpdatedEquals2(id)){
            sql = "update ApplicationPractice set topic = ?, description = ?,difficulty = ? where id = ?";
        }
        else {
            sql = "update ApplicationPractice set topic = ?, description = ?,difficulty = ?, updated = 1 where id = ?";
        }

        try {
            return runner.update(sql, topic, description, difficulty,id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * 插入应用题
     *
     * @param applicationPractice
     * @return:返回插入后的id
     */
    public int insertApplicationPractice(ApplicationPractice applicationPractice) {

        QueryRunner runner = new QueryRunner(getDataSource());
            String sql = "insert into ApplicationPractice(topic,description,difficulty,knowledgeId,sceneId,sequence,problemId,pic,type,updated) values(?,?,?,?,?,?,?,?,?,?)";

        try {
            Long result = runner.insert(sql, new ScalarHandler<>(), applicationPractice.getTopic(), applicationPractice.getDescription(), applicationPractice.getDifficulty(), applicationPractice.getKnowledgeId()
                    , applicationPractice.getSceneId(), applicationPractice.getSequence(), applicationPractice.getProblemId(), applicationPractice.getPic(), applicationPractice.getType(), applicationPractice.getUpdated());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }


    /**
     * 判断之前Updated字段的值是否是2
     * @param id
     * @return
     */
    private boolean isOriginUpdatedEquals2 (int id){
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<ApplicationPractice> h = new BeanHandler<>(ApplicationPractice.class);
        String sql = "select updated from ApplicationPractice where id = ?";
        try {
            ApplicationPractice applicationPractice = runner.query(sql,h,id);

            if(applicationPractice.getUpdated()==2){
                return true;
            }
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return false;
    }
}
