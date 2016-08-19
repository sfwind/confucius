package com.dianping.ba.base.framework.biz.service;

import com.dianping.ba.base.framework.biz.TestBase;
import com.dianping.ba.base.framework.biz.domain.hello.HelloDto;
import com.dianping.ba.base.framework.biz.domain.hello.HelloManager;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by qianyuhang on 7/15/15.
 */
public class HelloServiceTest extends TestBase {
    @Autowired
    private HelloManager helloManager;

//    @Test
    public void sendTest(){
        HelloDto helloDto = new HelloDto();
        helloDto.setEmployeeNumber("0009562");
        Assert.assertTrue(helloManager.hello(helloDto).equals("hello null!"));
    }
}
