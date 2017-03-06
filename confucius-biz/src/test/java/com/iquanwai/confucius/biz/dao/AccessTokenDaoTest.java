package com.iquanwai.confucius.biz.dao;

import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.dao.fragmentation.FragmentAnalysisDataDao;
import com.iquanwai.confucius.biz.dao.wx.AccessTokenDao;
import com.iquanwai.confucius.biz.util.Constants;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 17/2/22.
 */
public class AccessTokenDaoTest extends TestBase {
    @Autowired
    private AccessTokenDao accessTokenDao;
    @Autowired
    private FragmentAnalysisDataDao fragmentAnalysisDataDao;

    @Test
    public void testInsert() {
        accessTokenDao.insertOrUpdate("9uaMU9ItPjEVp0X1I4ZXkQmqTchOlKGs4ka77qw6ygPbY14b_Fbr4q4bRFGkcGm7_sQlYt4r_HyXEQBkEDPpa6obcCYYL3q_TzfbascmyTpeqynQLkO6OndfGi8f7SdSGTXfAIATGK");
    }

    @Test
    public void addTest() {
        fragmentAnalysisDataDao.insertArticleViewInfo(Constants.ViewInfo.Module.CHALLENGE, 1);

    }
}
