package com.iquanwai.confucius.biz.service;


import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nethunder on 2017/4/7.
 */
public class ThreadSafeTest {
    @Test
    public void test() throws InterruptedException {

    }

}

class RiseMemberCountRepo {
    private AtomicInteger remainCount = new AtomicInteger(500);
    private List<String> prepareMember = Lists.newArrayList();

    public void signup(String openId){

    }

}


