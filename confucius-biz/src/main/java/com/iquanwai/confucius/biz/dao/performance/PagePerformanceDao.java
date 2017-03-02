package com.iquanwai.confucius.biz.dao.performance;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.performance.PagePerformance;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

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
                "Dom, Frontend, `Load`, DomReady, Interactive, Ttf, Ttr, TtDns, TtConnect, Ttfb, " +
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
}
