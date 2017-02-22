package com.iquanwai.confucius.biz.domain.weixin.accessToken;


import com.iquanwai.confucius.biz.dao.wx.AccessTokenDao;
import com.iquanwai.confucius.biz.po.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AccessTokenServiceImpl implements AccessTokenService {
    private static String accessToken;
    protected static Logger logger = LoggerFactory.getLogger(AccessTokenService.class);
    @Autowired
    private WeiXinAccessTokenRepo weiXinAccessTokenRepo;
    @Autowired
    private AccessTokenDao accessTokenDao;

    public String getAccessToken() {
        if(accessToken!=null){
            return accessToken;
        }

        AccessToken token = accessTokenDao.load(AccessToken.class, 1);
        if(token==null){
            logger.info("insert access token");
            accessTokenDao.insertOrUpdate(_getAccessToken());
            return accessToken;
        }
        return token.getAccessToken();
    }

    private String _getAccessToken() {
        logger.info("refreshing access token");
        String strAccessToken = weiXinAccessTokenRepo.getAccessToken();
        if(strAccessToken!=null){
            accessToken = strAccessToken;
        }
        return accessToken;
    }

    public String refreshAccessToken() {
        String accessToken = _getAccessToken();
        accessTokenDao.insertOrUpdate(accessToken);

        return accessToken;
    }
}
