package com.iquanwai.confucius.biz.domain.weixin.message;

import org.w3c.dom.Document;

/**
 * Created by justin on 17/7/6.
 */
public interface CallbackMessageService {
    String handleCallback(Document document);
}
