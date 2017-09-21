package com.iquanwai.confucius.biz.service;

import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 16/9/16.
 */
public class PayServiceTest extends TestBase {
    @Autowired
    private PayService payService;
    @Autowired
    private SignupService signupService;


}
