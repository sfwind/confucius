package com.iquanwai.confucius.web.pc.backend.controller;

import com.iquanwai.confucius.biz.domain.backend.AutoMessageService;
import com.iquanwai.confucius.biz.po.AutoReplyMessage;
import com.iquanwai.confucius.biz.po.SubscribeMessage;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.util.List;
import java.util.Map;

/**
 * Created by 三十文 on 2017/9/26
 */
@RestController
@RequestMapping("/pc/operation/message")
public class AutoMessageController {

    @Autowired
    private AutoMessageService autoMessageService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/reply/load/text")
    public ResponseEntity<Map<String, Object>> loadTextAutoReplyMessage() {
        List<AutoReplyMessage> replyMessageList = autoMessageService.loadTextAutoReplyMessage();
        return WebUtils.result(replyMessageList);
    }

    @RequestMapping(value = "/reply/add", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> insertAutoReplyMessage(@RequestBody AutoReplyMessage autoReplyMessage) {
        AutoReplyMessage defaultMessage = autoMessageService.loadDefaultTextAutoReplyMessage();
        if (defaultMessage != null && autoReplyMessage.getIsDefault()) {
            return WebUtils.error("默认回复数据重复");
        }

        AutoReplyMessage replyMessage = autoMessageService.insertAutoReplyMessage(autoReplyMessage);
        if (replyMessage != null) {
            return WebUtils.result(replyMessage);
        } else {
            return WebUtils.error("数据新增失败");
        }
    }

    @RequestMapping(value = "/reply/update", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updateAutoReplyMessage(@RequestBody AutoReplyMessage autoReplyMessage) {
        AutoReplyMessage defaultMessage = autoMessageService.loadDefaultTextAutoReplyMessage();
        if (defaultMessage != null && autoReplyMessage.getIsDefault()) {
            return WebUtils.error("默认回复数据重复");
        }

        AutoReplyMessage replyMessage = autoMessageService.updateAutoReplyMessage(autoReplyMessage);
        if (replyMessage != null) {
            return WebUtils.result(replyMessage);
        } else {
            return WebUtils.error("数据更新失败");
        }
    }

    @RequestMapping(value = "/reply/del", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> deleteAutoReplyMessage(@PathParam("id") Integer id) {
        boolean result = autoMessageService.deleteAutoReplyMessage(id);
        if (result) {
            return WebUtils.success();
        } else {
            return WebUtils.error("数据删除失败");
        }
    }

    @RequestMapping(value = "/subscribe/load")
    public ResponseEntity<Map<String, Object>> loadSubscribeDefaultTextMessage() {
        return WebUtils.result(autoMessageService.loadSubscribeDefaultTextMessages());
    }

    @RequestMapping(value = "/subscribe/update")
    public ResponseEntity<Map<String, Object>> updateSubscribeDefaultTextMessage(@RequestBody SubscribeMessage subscribeMessage) {
        SubscribeMessage message = autoMessageService.updateSubscribeDefaultTextMessage(subscribeMessage);
        if (message != null) {
            return WebUtils.result(message);
        } else {
            return WebUtils.error("关注更新失败");
        }
    }

}
