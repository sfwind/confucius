package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.RiseOrder;
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
 * Created by nethunder on 2017/4/6.
 */
@Repository
public class RiseOrderDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(RiseOrder riseOrder){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into RiseOrder(OrderId, ProfileId, Openid, MemberType, Entry, IsDel) " +
                " VALUES (?, ?, ?, ?, ?, ?)";
        try{
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    riseOrder.getOrderId(), riseOrder.getProfileId(), riseOrder.getOpenid(),
                    riseOrder.getMemberType(), riseOrder.getEntry(), riseOrder.getIsDel());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public void closeOrder(String orderId){
        QueryRunner run = new QueryRunner(getDataSource());
        String sql = "Update RiseOrder set IsDel=1 where OrderId=?";
        try {
            run.update(sql,orderId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }


    public RiseOrder loadOrder(String orderId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<RiseOrder> h = new BeanHandler<>(RiseOrder.class);

        try {
            RiseOrder order = run.query("SELECT * FROM RiseOrder where OrderId=? ", h, orderId);
            return order;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public void entry(String orderId){
        QueryRunner run = new QueryRunner(getDataSource());
        String sql = "Update RiseOrder SET Entry = 1 where OrderId = ?";
        try{
            run.update(sql, orderId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public Integer loadHolderCount(){
        QueryRunner run = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<Long>();
        String sql = "Select count(distinct OpenId) from RiseOrder where Entry = 0 and IsDel = 0";
        try{
            return run.query(sql,h).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<RiseOrder> loadActiveOrder(){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<RiseOrder>> h = new BeanListHandler<RiseOrder>(RiseOrder.class);
        String sql = "Select * from RiseOrder  where Entry = 0 and IsDel = 0";
        try{
            return run.query(sql,h);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer userNotCloseOrder(String openId){
        QueryRunner run = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<Long>();
        String sql = "select count(1) from RiseOrder where OpenId = ? and Entry = 0 and IsDel = 0";
        try{
            return run.query(sql,h,openId).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

}
