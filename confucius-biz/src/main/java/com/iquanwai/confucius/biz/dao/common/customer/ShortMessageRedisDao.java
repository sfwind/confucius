package com.iquanwai.confucius.biz.dao.common.customer;

import com.iquanwai.confucius.biz.dao.RedisUtil;
import com.iquanwai.confucius.biz.domain.message.SendLimit;
import com.iquanwai.confucius.biz.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created by nethunder on 2017/6/14.
 */
@Repository
public class ShortMessageRedisDao extends RedisUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String MIN_SEND_KEY = "short:min:{key}";
    private static final String HOUR_SEND_KEY = "short:hour:{key}";
    private static final String DAY_SEND_KEY = "short:day:{key}";

    @Autowired
    private RedisUtil redisUtil;

    public SendLimit getUserSendLimit(Integer profileId){
        if (profileId == null) {
            return null;
        }
        Integer minSend = redisUtil.getInt(MIN_SEND_KEY.replace("{key}", profileId.toString()), 0);
        Integer hourSend = redisUtil.getInt(HOUR_SEND_KEY.replace("{key}", profileId.toString()), 0);
        Integer daySend = redisUtil.getInt(DAY_SEND_KEY.replace("{key}", profileId.toString()), 0);

        SendLimit limit = new SendLimit();
        limit.setProfileId(profileId);
        limit.setMinSend(minSend);
        limit.setHourSend(hourSend);
        limit.setDaySend(daySend);
        return limit;
    }


    public void addSendCount(Integer profileId) {
        SendLimit userSendLimit = getUserSendLimit(profileId);
        String minKey = MIN_SEND_KEY.replace("{key}", profileId.toString());
        String hourKey = HOUR_SEND_KEY.replace("{key}", profileId.toString());
        String dayKey = DAY_SEND_KEY.replace("{key}", profileId.toString());
        Long minExpired = 0L;
        Long hourExpired = 0L;
        Long dayExpired = 0L;
        if (userSendLimit.getMinSend() == 0) {
            minExpired = 60L;
        } else {
            minExpired = redisUtil.getRemainTime(minKey);
        }
        if (userSendLimit.getHourSend() == 0) {
            hourExpired = 60 * 60L;
        } else {
            hourExpired = redisUtil.getRemainTime(hourKey);
        }
        if (userSendLimit.getDaySend() == 0) {
            dayExpired = DateUtils.nextDayRemainSeconds(new Date());
        } else {
            dayExpired = redisUtil.getRemainTime(dayKey);
        }

        redisUtil.set(minKey, userSendLimit.getMinSend() + 1, minExpired);
        redisUtil.set(hourKey, userSendLimit.getHourSend() + 1, hourExpired);
        redisUtil.set(dayKey, userSendLimit.getDaySend() + 1, dayExpired);
    }
}

