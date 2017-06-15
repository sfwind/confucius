package com.iquanwai.confucius.biz.dao.common.customer;

import com.iquanwai.confucius.biz.dao.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * Created by nethunder on 2017/6/14.
 */
@Repository
public class ShotMessageRedisDao extends RedisUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void getShowMessageLimit(){

    }
}

