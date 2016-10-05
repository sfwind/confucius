package com.iquanwai.confucius.biz.service;

import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 16/10/5.
 */
public class SignupServiceTest extends TestBase {
    @Autowired
    private SignupService signupService;

    @Test
    public void sendWelcomeMessageTest(){
        signupService.sendWelcomeMsg(1, "o5h6ywlXxHLmoGrLzH9Nt7uyoHbM", 1);
    }
}
