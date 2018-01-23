package com.iquanwai.confucius.biz.service;

import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.util.RestfulHelper;
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

    @Test
    public void testPost() throws IOException {
        oAuthService.weMiniAccessToken("0110dmI2175E1O1IMZH21jeoI210dmIZ");
    }

}
