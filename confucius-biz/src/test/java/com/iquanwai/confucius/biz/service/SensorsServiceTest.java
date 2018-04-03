package com.iquanwai.confucius.biz.service;

import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseClassMemberDao;
import com.iquanwai.confucius.biz.dao.wx.FollowUserDao;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
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

    @Test
    public void profileTest() throws InvalidArgumentException {
//        昵称	nickname	字符串
//        微信id	weixinid	字符串
//        姓名	realname	字符串
//        性别	sex	字符串
//        所在省份	province	字符串
//        所在城市	city	字符串
//        详细地址	address	字符串
//        邮箱	email	字符串
//        手机	mobileNo	字符串
//        婚恋情况	married	字符串
//        工作年限		字符串
//        参加工作年份		字符串
//        首次访问时间	firstVisitedTime	日期
//        注册渠道	subscribeChannel	字符串
//        最后参加的活动渠道	finalChannel	字符串
//        用户角色		数值
//        累积学习体验课次数	experienceCount	数值
//        班级	className	字符串
//        小组	groupId	字符串
        for (Profile profile : profileDao.loadAll(Profile.class)) {
            RiseClassMember member = riseClassMemberDao.loadActiveRiseClassMember(profile.getId());
            RiseMember riseMember = riseMemberDao.loadValidRiseMember(profile.getId());
            Account account = followUserDao.queryByUnionId(profile.getUnionid());
            profileSet(profile, "nickname", profile.getNickname());
            profileSet(profile, "weixinid", profile.getWeixinId());
            profileSet(profile, "realname", profile.getRealName());
            if (account != null) {
                sa.profileSet(profile.getRiseId(), true, "sex", account.getSex());
            }
            profileSet(profile, "province", profile.getProvince());
            profileSet(profile, "city", profile.getCity());
            profileSet(profile, "address", profile.getAddress());
            profileSet(profile, "email", profile.getEmail());
            profileSet(profile, "mobileNo", profile.getMobileNo());
            profileSet(profile, "married", profile.getMarried());
            profileSet(profile, "workingLife", profile.getWorkingLife());
            profileSet(profile, "workingYear", profile.getWorkYear());
            sa.profileSet(profile.getRiseId(), true, "rolename", riseMember == null ? 0 : riseMember.getMemberTypeId());
            if (member != null && member.getClassName() != null) {
                profileSet(profile, "className", member.getClassName());
            }
            if (member != null && member.getGroupId() != null) {
                profileSet(profile, "groupId", member.getClassName());
            }

        }
    }

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
