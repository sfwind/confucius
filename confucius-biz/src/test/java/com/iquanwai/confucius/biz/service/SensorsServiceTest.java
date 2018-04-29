package com.iquanwai.confucius.biz.service;

import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseClassMemberDao;
import com.iquanwai.confucius.biz.dao.wx.FollowUserDao;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class SensorsServiceTest extends TestBase {
    @Autowired
    private SensorsAnalytics sa;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private RiseClassMemberDao riseClassMemberDao;
    @Autowired
    private FollowUserDao followUserDao;
    @Autowired
    private RiseMemberDao riseMemberDao;

    private void profileSet(Profile profile, String key, String value) throws InvalidArgumentException {
        if (value == null) {
            return;
        }
        System.out.println(profile.getRiseId() + ":" + key + ":" + value);
        sa.profileSet(profile.getRiseId(), true, key, value);

    }

    @Test
    public void test() throws InvalidArgumentException {
        //        首次访问时间	firstVisitedTime	日期
//        注册渠道	subscribeChannel	字符串
//        最后参加的活动渠道	finalChannel	字符串
//        用户角色		数值
//        累积学习体验课次数	experienceCount	数值
        sa.profileSet("r5h7dt1", true, "firstVisitedTime", new DateTime().toDate());
        sa.profileSet("r5h7dt1", true, "subscribeChannel", "test");
        sa.profileSet("r5h7dt1", true, "finalChannel", "freeLimit");
        sa.profileSet("r5h7dt1", true, "experienceCount", 3);

    }
}
