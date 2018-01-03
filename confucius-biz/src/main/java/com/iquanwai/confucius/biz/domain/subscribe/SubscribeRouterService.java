package com.iquanwai.confucius.biz.domain.subscribe;

import com.iquanwai.confucius.biz.po.common.customer.SubscribeRouterConfig;

public interface SubscribeRouterService {
    /**
     * 根据当前路径获取关注页面的二维码配置
     */
    SubscribeRouterConfig loadUnSubscribeRouterConfig(String currentPatchName, String followKey);

    /**
     * 根据场景号获取生成二维码的 base64
     */
    String loadSubscribeQrCode(String scene);
}
