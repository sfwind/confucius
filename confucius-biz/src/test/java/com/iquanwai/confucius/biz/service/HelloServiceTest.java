package com.iquanwai.confucius.biz.service;

import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.util.zk.ConfigNode;
import com.iquanwai.confucius.biz.util.zk.ZKConfigUtils;
import org.junit.Test;

import java.util.List;

/**
 * Created by justin on 7/15/15.
 */
public class HelloServiceTest extends TestBase {


    @Test
    public void sendTest(){
        ZKConfigUtils configUtils = new ZKConfigUtils();
        List<ConfigNode> configNodeList = configUtils.getAllValue("rise");
    }
}
