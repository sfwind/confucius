package com.iquanwai.confucius.biz.domain.subscribe;

import com.iquanwai.confucius.biz.po.common.customer.SubscribeRouterConfig;

public interface SubscribeRouterService {
    SubscribeRouterConfig loadUnSubscribeRouterConfig(String currentPatchName);

    String loadSubscribeQrCode(String scene);
}
