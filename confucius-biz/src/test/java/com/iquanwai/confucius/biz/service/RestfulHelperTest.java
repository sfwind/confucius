package com.iquanwai.confucius.biz.service;

import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * Created by justin on 16/9/15.
 */
public class RestfulHelperTest extends TestBase {
    @Autowired
    private RestfulHelper restfulHelper;
    @Autowired
    private OAuthService oAuthService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private SensorsAnalytics sa;


    @Test
    public void testPost() throws IOException {
        oAuthService.weMiniAccessToken("0110dmI2175E1O1IMZH21jeoI210dmIZ");
    }
    @Test
    public void opsTest(){
        operationLogService.trace(25556, "test");

    }

}
