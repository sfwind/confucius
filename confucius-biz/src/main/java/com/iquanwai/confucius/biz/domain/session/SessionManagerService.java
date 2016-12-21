package com.iquanwai.confucius.biz.domain.session;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by nethunder on 2016/12/19.
 */
public interface SessionManagerService {

    /**
     * 将sessionId添加到等待列表
     */
    Pair<Integer,String> waitLogin(String sessionId);

    /**
     * 获取已经登录的用户
     */
    User getLoginUser(String sessionId);

    /**
     * 是否已经登录
     */
    boolean isLogined(String sessionId);

    /**
     * 是否在等待登录
     */
    boolean isWaitLogin(String sessionId);

    /**
     * 从等待登录／已经登录列表都删掉sessionid
     */
    void removeUserSession(String sessionId);


}
