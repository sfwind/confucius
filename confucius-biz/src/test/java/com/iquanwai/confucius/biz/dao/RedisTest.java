package com.iquanwai.confucius.biz.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.dao.common.customer.ShortMessageRedisDao;
import com.iquanwai.confucius.biz.domain.message.ShortMessageService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import org.junit.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * Created by nethunder on 2017/4/26.
 */
public class RedisTest extends TestBase {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ShortMessageService shortMessageService;
    @Autowired
    private ShortMessageRedisDao shortMessageRedisDao;

    /**
     * 测试
     * @throws InterruptedException
     */
    @Test
    public void test() throws InterruptedException {
        new Thread(()->{
            long thatTime = System.currentTimeMillis();
            Integer time = 1;
            while(true){
                long curTime = System.currentTimeMillis();
                if ((curTime - thatTime) / 1000 >= 1) {
                    thatTime = curTime;
                    log("时间:" + (time++) + "s");
                }
            }
        }).start();

        log("test lock");
        new Thread(()->{
            redisUtil.lock("test:flag",(lock)->{
                log("线程1进入锁");
                try {
                    Thread.sleep(2000l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log("pc-token:"+redisUtil.get("accessToken:pc"));
                log("线程1离开锁");
            });
        }).start();
        new Thread(()->{
            redisUtil.lock("test:flag",(lock)->{
                log("线程2进入锁");
                try {
                    Thread.sleep(2000l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log("pc-token:"+redisUtil.get("accessToken:pc"));
                log("线程2离开锁");
            });
        }).start();

        log("测试设置字符串");
        redisUtil.set("test:name","张三");
        Assert.isTrue("张三".equals(redisUtil.get("test:name")));
        log("测试设置数字");
        redisUtil.set("test:Number", 4);
        Assert.isTrue("4".equals(redisUtil.get("test:Number")));
        log("测试设置对象");
        Profile profile = new Profile();
        profile.setNickname("薛定谔的猫");
        redisUtil.set("test:profile", profile);
        Assert.isTrue("薛定谔的猫".equals(redisUtil.get("test:profile", Profile.class).getNickname()));
        log("测试过期时间设置");
        redisUtil.set("test:expired",true,5l);
        Assert.isTrue("true".equals(redisUtil.get("test:expired")));
        Thread.sleep(7000l);
        Assert.isNull(redisUtil.get("test:expired"));
        log("测试全部结束");
    }


    @Test
    public void testKeys(){
        // 先设置一堆key
        for(int i=0;i<300; i++) {
            redisUtil.set("test:key:" + i, i);
        }
        // 获取正则获取key
        Iterable<String> keysByPattern = redisUtil.getKeysByPattern("test:key:*");
        Integer number = 0;
        for (String aKeysByPattern : keysByPattern) {
            log(aKeysByPattern + "===" + (++number));
        }

        long l = redisUtil.deleteByPattern("test:key:*");
        log("删除了" + l + "行");
    }

    @Test
    public void setTest() throws InterruptedException {
        new Thread(()->{
            redisUtil.lock("flag", lock -> {
                Map<String, Object> map = Maps.newHashMap();
                log("a 加锁");
                map.get("test").getClass();
            });
        }).start();

        new Thread(()->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            redisUtil.lock("flag",lock -> {
                log("b 加锁");
            });
        }).start();


        Thread.sleep(10000);
    }

    @Test
    public void numberTet() throws InterruptedException {
        redisUtil.set("tet",3,7l);
        RedissonClient redissonClient = redisUtil.getRedissonClient();



        new Thread(()->{
            long thatTime = System.currentTimeMillis();
            Integer time = 1;
            while(true){
                long curTime = System.currentTimeMillis();
                if ((curTime - thatTime) / 1000 >= 1) {
                    thatTime = curTime;
                    log("时间:" + (time++) + "s");
                    RBucket<Object> bucket = redissonClient.getBucket("tet");
                    long remain = bucket.remainTimeToLive();
                    log("剩余过期时间:" + remain);
                }
            }
        }).start();
        Thread.sleep(10000l);
    }

    @Test
    public void shortMessageTest() throws InterruptedException {
        List<String> phoneList = Lists.newArrayList();
        phoneList.add("13122535350");
//        shortMessageService.sendMessage(2333, phoneList);
    }

}
