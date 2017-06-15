package com.iquanwai.confucius.biz.dao;

import com.alibaba.fastjson.JSONObject;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by nethunder on 2017/4/26.
 */

@Repository
public class RedisUtil {
    private RedissonClient redissonClient;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public void setRedissonClient(RedissonClient redissonClient){
        this.redissonClient = redissonClient;
    }

    public RedissonClient getRedissonClient(){
        return this.redissonClient;
    }

    /**
     * 默认获得字符串类型的数据
     * @param key key
     */
    public String get(String key){
        return get(String.class, key);
    }

    /**
     * 获取对象
     * @param key key
     * @param clazz class
     */
    public <T> T get(String key,Class<T> clazz){
        String json = get(key);
        if (json == null) {
            return null;
        }
        return JSONObject.parseObject(json, clazz);
    }

    /**
     * 设置对象,可以直接设置字符串或者复杂类型
     * @param key key
     * @param value value
     */
    public void set(String key, Object value) {
        Assert.notNull(key, "message 不能为null");
        Assert.notNull(value, "object 不能为null");
        RBucket<String> bucket = redissonClient.getBucket(key);
        if(value instanceof String){
            bucket.set(value.toString());
        } else {
            String json = JSONObject.toJSONString(value);
            bucket.set(json);
        }
    }

    /**
     * 设置对象,可以直接设置字符串或者复杂类型
     * @param key key
     * @param value value
     * @param timeToExpired 过期时间，单位秒
     */
    public void set(String key,Object value,Long timeToExpired){
        Assert.notNull(key, "message 不能为null");
        Assert.notNull(value, "object 不能为null");
        RBucket<String> bucket = redissonClient.getBucket(key);
        String finalValue = null;
        if(value instanceof String){
            finalValue = value.toString();
        } else {
            finalValue =  JSONObject.toJSONString(value);
        }
        if (timeToExpired == null) {
            bucket.set(finalValue);
        } else {
            bucket.set(finalValue, timeToExpired, TimeUnit.SECONDS);
        }
    }


    public void lock(String key, Consumer<RLock> consumer) {
        RLock lock = redissonClient.getLock(key);
        logger.info("Thread {} want the lock", Thread.currentThread().getId());
        try {
            lock.tryLock(60, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        logger.info("Thread {} has lock :{}", Thread.currentThread().getId(), lock.isHeldByCurrentThread());
        consumer.accept(lock);
        logger.info("Thread {} will release the lock",Thread.currentThread().getId());
        lock.unlock();
        logger.info("Thread {} don't have the lock :{}", Thread.currentThread().getId(), lock.isHeldByCurrentThread());
    }

    private <T> T get(Class<T> tClass, String key) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }
}