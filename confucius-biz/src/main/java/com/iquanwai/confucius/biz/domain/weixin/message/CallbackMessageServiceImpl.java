package com.iquanwai.confucius.biz.domain.weixin.message;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

/**
 * Created by justin on 17/7/6.
 */
@Service
public class CallbackMessageServiceImpl implements CallbackMessageService {
    private static final String SUCCESS = "success";
    @Override
    public String handleCallback(Document document) {
        return SUCCESS;
    }
}
