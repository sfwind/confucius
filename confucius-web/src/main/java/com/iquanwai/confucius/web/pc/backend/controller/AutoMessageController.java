package com.iquanwai.confucius.web.pc.backend.controller;

import com.iquanwai.confucius.biz.domain.backend.AutoMessageService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.AutoReplyMessage;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.SubscribeMessage;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
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
    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping("/reply/load/text")
    public ResponseEntity<Map<String, Object>> loadTextAutoReplyMessage(PCLoginUser loginUser) {
        List<AutoReplyMessage> replyMessageList = autoMessageService.loadTextAutoReplyMessage();
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("运营")
                .function("自动回复")
                .action("加载自动回复");
        operationLogService.log(operationLog);
        return WebUtils.result(replyMessageList);
    }

    @RequestMapping(value = "/reply/add", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> insertAutoReplyMessage(PCLoginUser loginUser,
                                                                      @RequestBody AutoReplyMessage autoReplyMessage) {
        AutoReplyMessage defaultMessage = autoMessageService.loadDefaultTextAutoReplyMessage();
        if (defaultMessage != null && autoReplyMessage.getIsDefault()) {
            return WebUtils.error("默认回复数据重复");
        }
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("运营")
                .function("自动回复")
                .action("添加自动回复");
        operationLogService.log(operationLog);
        AutoReplyMessage replyMessage = autoMessageService.insertAutoReplyMessage(autoReplyMessage);
        if (replyMessage != null) {
            return WebUtils.result(replyMessage);
        } else {
            return WebUtils.error("数据新增失败");
        }
    }

    @RequestMapping(value = "/reply/update", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updateAutoReplyMessage(PCLoginUser loginUser,
                                                                      @RequestBody AutoReplyMessage autoReplyMessage) {
        AutoReplyMessage defaultMessage = autoMessageService.loadDefaultTextAutoReplyMessage();
        if (defaultMessage != null && autoReplyMessage.getIsDefault()) {
            return WebUtils.error("默认回复数据重复");
        }
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("运营")
                .function("自动回复")
                .action("更新自动回复");
        operationLogService.log(operationLog);
        AutoReplyMessage replyMessage = autoMessageService.updateAutoReplyMessage(autoReplyMessage);
        if (replyMessage != null) {
            return WebUtils.result(replyMessage);
        } else {
            return WebUtils.error("数据更新失败");
        }
    }

    @RequestMapping(value = "/reply/del", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> deleteAutoReplyMessage(PCLoginUser loginUser,
                                                                      @PathParam("id") Integer id) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("运营")
                .function("自动回复")
                .action("删除自动回复");
        operationLogService.log(operationLog);
        boolean result = autoMessageService.deleteAutoReplyMessage(id);
        if (result) {
            return WebUtils.success();
        } else {
            return WebUtils.error("数据删除失败");
        }
    }

    @RequestMapping(value = "/subscribe/load")
    public ResponseEntity<Map<String, Object>> loadSubscribeDefaultTextMessage(PCLoginUser loginUser) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("运营")
                .function("关注回复")
                .action("加载关注回复");
        operationLogService.log(operationLog);
        return WebUtils.result(autoMessageService.loadSubscribeDefaultTextMessages());
    }

    @RequestMapping(value = "/subscribe/update")
    public ResponseEntity<Map<String, Object>> updateSubscribeDefaultTextMessage(PCLoginUser loginUser,
                                                                                 @RequestBody SubscribeMessage subscribeMessage) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("运营")
                .function("关注回复")
                .action("更新关注回复");
        operationLogService.log(operationLog);
        SubscribeMessage message = autoMessageService.updateSubscribeDefaultTextMessage(subscribeMessage);
        if (message != null) {
            return WebUtils.result(message);
        } else {
            return WebUtils.error("关注更新失败");
        }
    }

}
