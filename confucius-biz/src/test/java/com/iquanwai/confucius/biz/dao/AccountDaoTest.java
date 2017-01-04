package com.iquanwai.confucius.biz.dao;

import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.dao.wx.FollowUserDao;
import com.iquanwai.confucius.biz.po.Account;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 17/1/3.
 */
public class AccountDaoTest extends TestBase {
    @Autowired
    private FollowUserDao followUserDao;

    @Test
    public void testEmoji(){
        Account account = followUserDao.queryByOpenid("o5h6ywrodsSSSxck7CjASBPdhOv4");

        account.setNickname("\uD83C\uDF47雷立风行");
        followUserDao.updateInfo(account);
    }
}
