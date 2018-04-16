package com.iquanwai.confucius.biz.service;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.dao.RedisUtil;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.fragmentation.FragmentClassMemberDao;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.customer.CustomerMessageService;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 7/15/15.
 */
public class HelloServiceTest extends TestBase {

    @Autowired
    private SignupService signupService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private FragmentClassMemberDao fragmentClassMemberDao;


    @Test
    public void test() {
        List<RiseMember> riseMembers = riseMemberDao.loadAll(RiseMember.class);
        Map<Integer, Boolean> start = Maps.newHashMap();
        start.put(1, true);
        for (int i = 0; i < riseMembers.size(); i++) {
            RiseMember riseMember = riseMembers.get(i);
            new Thread(() -> {
                while (start.get(1)) {
                    //ignore
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                signupService.insertClassMemberMemberId(riseMember.getProfileId(), riseMember.getMemberTypeId());
                System.out.println("complete");
            }).start();
        }
        start.put(1, false);
        try {
            Thread.sleep(1000 * 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


}
