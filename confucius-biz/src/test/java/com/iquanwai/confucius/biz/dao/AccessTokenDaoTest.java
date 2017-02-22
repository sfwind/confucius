package com.iquanwai.confucius.biz.dao;

import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.dao.wx.AccessTokenDao;
import com.iquanwai.confucius.biz.po.Account;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 17/2/22.
 */
public class AccessTokenDaoTest extends TestBase {
    @Autowired
    private AccessTokenDao accessTokenDao;

    @Test
    public void testInsert(){
        accessTokenDao.insertOrUpdate("123");
    }
}
