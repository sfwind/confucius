package com.iquanwai.confucius.biz.domain.weixin.accessToken;


public interface AccessTokenService {
    String getAccessToken();

    String refreshAccessToken(boolean force);
}
