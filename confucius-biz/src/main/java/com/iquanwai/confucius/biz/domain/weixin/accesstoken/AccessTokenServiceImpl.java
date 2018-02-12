package com.iquanwai.confucius.biz.domain.weixin.accesstoken;

import com.iquanwai.confucius.biz.dao.RedisUtil;
import com.iquanwai.confucius.biz.dao.wx.AccessTokenDao;
import com.iquanwai.confucius.biz.domain.weixin.api.WeiXinApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenServiceImpl implements AccessTokenService {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private AccessTokenDao accessTokenDao;
    @Autowired
    private WeiXinApiService weiXinApiService;

    private static String accessToken;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String getAccessToken() {
        if (accessToken != null) {
            return accessToken;
        }
        String token = redisUtil.get("accessToken");
        if (token == null) {
            logger.info("insert access token");
            String accessToken = _getAccessToken();
            redisUtil.set("accessToken", accessToken);
            accessTokenDao.insertOrUpdate(accessToken);
        } else {
            accessToken = token;
        }
        return accessToken;
    }

    @Override
    public String refreshAccessToken(boolean force) {
        if (force) {
            forceUpdateAccessToken();
        } else {
            String token = redisUtil.get("accessToken");
            if (token == null) {
                logger.info("insert access token");
                String accessToken = _getAccessToken();
                redisUtil.set("accessToken", accessToken);
                accessTokenDao.insertOrUpdate(accessToken);
            } else {
                if (token.equals(accessToken)) {
                    forceUpdateAccessToken();
                } else {
                    accessToken = token;
                    logger.info("reload access token");
                }
            }
        }
        return accessToken;
    }

    private String _getAccessToken() {
        logger.info("refreshing access token");
        String strAccessToken = weiXinApiService.getAppAccessToken();
        if (strAccessToken != null) {
            accessToken = strAccessToken;
        }
        return accessToken;
    }

    private void forceUpdateAccessToken() {
        String accessToken = _getAccessToken();
        redisUtil.set("accessToken", accessToken);
        accessTokenDao.insertOrUpdate(accessToken);
    }
}
