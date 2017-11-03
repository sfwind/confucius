package com.iquanwai.confucius.biz.domain.weixin.accesstoken;


public interface AccessTokenService {
    String getAccessToken();

    String refreshAccessToken(boolean force);
}
