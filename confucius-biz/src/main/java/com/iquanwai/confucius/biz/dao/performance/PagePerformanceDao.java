package com.iquanwai.confucius.biz.dao.performance;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.performance.PagePerformance;
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
 * Created by yongqiang.shen on 17/3/2.
 */
@Repository
public class PagePerformanceDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void entry(PagePerformance pagePerformance) {
        if (pagePerformance.getApp() == null || "".equals(pagePerformance.getApp())) {
            return;
        }
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "INSERT INTO PagePerformance(App, Url, Screen, Viewport, Uuid," +
                " CookieSize, Redirect, Dns, Connect, Network, Send, Receive, Backend, Render, " +
                "Dom, Frontend, `Load`, DomReady, Interactive, Ttf, Ttr, Ttdns, Ttconnect, Ttfb, " +
                "Status) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?," +
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";
        try {
            run.insert(insertSql, new ScalarHandler<>(),
                    pagePerformance.getApp(), pagePerformance.getUrl(),
                    pagePerformance.getScreen(), pagePerformance.getViewport(),
                    pagePerformance.getUuid(), pagePerformance.getCookieSize(),
                    pagePerformance.getRedirect(), pagePerformance.getDns(),
                    pagePerformance.getConnect(), pagePerformance.getNetwork(),
                    pagePerformance.getSend(), pagePerformance.getReceive(),
                    pagePerformance.getBackend(), pagePerformance.getRender(),
                    pagePerformance.getDom(), pagePerformance.getFrontend(),
                    pagePerformance.getLoad(), pagePerformance.getDomReady(),
                    pagePerformance.getInteractive(), pagePerformance.getTtf(),
                    pagePerformance.getTtr(), pagePerformance.getTtdns(),
                    pagePerformance.getTtconnect(), pagePerformance.getTtfb());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<PagePerformance> queryAboutAddTime(String startTimeStr, String endTimeStr){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<PagePerformance>> h = new BeanListHandler<>(PagePerformance.class);
        String sql = "SELECT * FROM PagePerformance where AddTime >= ? and AddTime <= ? order by AddTime";
        try {
            return run.query(sql, h, startTimeStr, endTimeStr);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
