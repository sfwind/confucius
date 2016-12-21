package com.iquanwai.confucius.biz.domain.session;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.QRCodeUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by nethunder on 2016/12/19.
 */
@Service
public class SessionManagerServiceImpl implements SessionManagerService{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * Session存储结构，目前在缓存中，之后放倒redis
     */
    private Map<String,User> userMap = Maps.newConcurrentMap();
    private CopyOnWriteArrayList<String> waitLoginList = Lists.newCopyOnWriteArrayList();

    /**
     * ConcurrentMap不能有null，但是
     * @param sessionId
     * @return
     */
    @Override
    public Pair<Integer,String> waitLogin(String sessionId) {
        if(sessionId==null){
            //如果没有获取sessionid的话禁止登录
            return new ImmutablePair<Integer, String>(0, "登录时需要打开Cookie哦");
        }
        if(this.isLogined(sessionId)){
            return new ImmutablePair<Integer, String>(0, "您已经登录，请不要重复登录");
        } else {
            if(this.isWaitLogin(sessionId)){
                // 在等待登录
                return new ImmutablePair<Integer,String>(0,"您已经打开登录页面，请不要重复打开");
            }
            this.waitLoginList.add(sessionId);
            return new ImmutablePair<Integer,String>(1,"ok");
        }
    }

    @Override
    public User getLoginUser(String sessionId) {
        return this.userMap.get(sessionId);
    }

    @Override
    public boolean isLogined(String sessionId){
        return this.userMap.containsKey(sessionId);
    }


    /**
     * ？？ 线程安全？
     * @param sessionId
     * @return
     */
    @Override
    public boolean isWaitLogin(String sessionId){
        boolean isWait = false;
        isWait = this.waitLoginList.contains(sessionId);
        return isWait;
    }


    @Override
    public void removeUserSession(String sessionId) {
        if(sessionId!=null){
            this.waitLoginList.remove(sessionId);
            this.userMap.remove(sessionId);
        }
    }





}
