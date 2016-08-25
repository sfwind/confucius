package com.iquanwai.confucius.biz.dao;

import com.iquanwai.confucius.biz.dao.po.Callback;
import org.apache.ibatis.annotations.Param;

/**
 * Created by justin on 16/8/13.
 */
public interface CallbackDao {
    void insert(@Param("callback") Callback callback);

    void updateUserInfo(@Param("state") String state,
                        @Param("accessToken") String accessToken,
                        @Param("refreshToken") String refreshToken,
                        @Param("openid") String openid);

    Callback queryByState(@Param("state") String state);

    Callback queryByAccessToken(@Param("accessToken") String accessToken);

    void refreshToken(@Param("state") String state, @Param("accessToken") String newAccessToken);
}
