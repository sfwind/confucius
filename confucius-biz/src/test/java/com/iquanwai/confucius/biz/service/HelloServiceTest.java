package com.iquanwai.confucius.biz.service;

import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.customer.CustomerMessageService;
import com.iquanwai.confucius.biz.util.Constants;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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


    @Test
    public void test() {
        customerMessageService.sendCustomerMessage(accountService.getProfile(30).getOpenid(), "LiCBL-QjQYIpExWSJt9gUxLGt3vNdf9bqYuxe-QDt5A", Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
        customerMessageService.sendCustomerMessage(accountService.getProfile(30).getOpenid(), "LiCBL-QjQYIpExWSJt9gUxLGt3vNdf9bqYuxe-QDt5A", Constants.WEIXIN_MESSAGE_TYPE.TEXT);
    }

    @Test
    public void soutTest(){
        signupService.insertClassMemberMemberId(25556, 3);
    }


}
