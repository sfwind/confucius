package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.util.page.Page;
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
public class ApplicationSubmitDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(ApplicationSubmit applicationSubmit){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into ApplicationSubmit(Openid, ApplicationId, PlanId) " +
                "values(?,?,?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    applicationSubmit.getOpenid(), applicationSubmit.getApplicationId(),
                    applicationSubmit.getPlanId());
            return insertRs.intValue();
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 查询用户提交记录
     * @param applicationId 应用练习id
     * @param planId 计划id
     * @param openid  openid
     */
    public ApplicationSubmit load(Integer applicationId, Integer planId, String openid){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ApplicationSubmit> h = new BeanHandler(ApplicationSubmit.class);
        String sql = "SELECT * FROM ApplicationSubmit where Openid=? and ApplicationId=? and PlanId=?";
        try {
            return run.query(sql, h, openid, applicationId, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<ApplicationSubmit> load(Integer applicationId, String openid){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler(ApplicationSubmit.class);
        String sql = "SELECT * FROM ApplicationSubmit where Openid=? and ApplicationId=?";
        try {
            return run.query(sql, h, openid, applicationId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }



    public List<ApplicationSubmit> load(Integer applicationId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);
        String sql = "SELECT * FROM ApplicationSubmit where ApplicationId=? and Content is not null order by UpdateTime desc";
        try {
            return run.query(sql, h, applicationId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public boolean firstAnswer(Integer id, String content){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Content=?,PublishTime = CURRENT_TIMESTAMP where Id=?";
        try {
            runner.update(sql, content, id);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public boolean answer(Integer id, String content){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Content=? where Id=?";
        try {
            runner.update(sql, content, id);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public boolean updatePointStatus(Integer id){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set PointStatus=1 where Id=?";
        try {

            runner.update(sql, id);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }


    public List<ApplicationSubmit> getPracticeSubmit(Integer practiceId, Page page){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from ApplicationSubmit where ApplicationId=? and Content is not null order by UpdateTime desc limit "
                + page.getOffset() + "," + page.getLimit();
        ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);
        try {
            return runner.query(sql, h, practiceId);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<ApplicationSubmit> getSubmitByApplicationIds(List<Integer> applicationIds, int size){
        QueryRunner runner = new QueryRunner(getDataSource());
        String questionMark = produceQuestionMark(applicationIds.size());
        String sql = "select * from ApplicationSubmit where ApplicationId in ("+questionMark+
                ") and Content is not null and Feedback = 0 order by RequestFeedback desc, length desc limit "+size;
        ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);
        try {
            return runner.query(sql, h, applicationIds);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }


    public List<ApplicationSubmit> getHighlightSubmit(Integer practiceId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from ApplicationSubmit where ApplicationId=? and Priority=1 ";
        ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);
        try {
            return runner.query(sql, h, practiceId);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public void highlight(Integer submitId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Priority=1, HighlightTime = now() where Id=?";
        try {

            runner.update(sql, submitId);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void unHighlight(Integer submitId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Priority=0 where Id=?";
        try {

            runner.update(sql, submitId);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}
