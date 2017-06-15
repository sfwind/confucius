package com.iquanwai.confucius.biz.dao;

import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.po.AccessToken;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by nethunder on 2017/4/26.
 */
public class RedisTest extends TestBase {
    @Autowired
    private RedisUtil redisUtil;

    @Test
    public void test(){

        AccessToken token = new AccessToken();
        token.setAccessToken("fwefewfew");
//        redisUtil.set("accessToken", token);
//        log(redisUtil.get(AccessToken.class, "accessToken").getAccessToken());
        AccessToken act = redisUtil.get("act", AccessToken.class);
        System.out.println("act:" + (act == null ? "null" : act));
        redisUtil.set("fef",token,5l);
        try {
            Thread.sleep(6 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        AccessToken act1 = redisUtil.get("fef", AccessToken.class);
        System.out.println(act1.getAccessToken());
        System.out.println(act1.equals(token));
        System.out.println(act1 == token);

    }

    @Test
    public void setTest(){
//        redisUtil.set("a", "gsegsdfsdf");
//        log(redisUtil.get(String.class, "a"));
//        Profile profile = new Profile();
//        profile.setNickname("薛定谔的猫");
//
//        redisUtil.set("user:xue", profile);

//        log(redisUtil.get(Profile.class, "user:xue").getNickname());

//        log(redisUtil.get(Profile.class, "user:xue").getNickname());

//        redisUtil.set("a", "fwefewf");

//        log(redisUtil.get(String.class, "a"));



    }
}
