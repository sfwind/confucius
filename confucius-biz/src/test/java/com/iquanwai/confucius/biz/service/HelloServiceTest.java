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


}
