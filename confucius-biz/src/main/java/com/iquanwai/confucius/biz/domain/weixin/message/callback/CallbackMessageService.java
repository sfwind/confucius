package com.iquanwai.confucius.biz.domain.weixin.message.callback;

import org.w3c.dom.Document;

/**
 * Created by justin on 17/7/6.
 */
public interface CallbackMessageService {
    /**
     * 处理消息回调
     * */
    String handleCallback(Document document);
}
