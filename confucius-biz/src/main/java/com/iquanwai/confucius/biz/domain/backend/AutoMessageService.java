package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.po.AutoReplyMessage;
import com.iquanwai.confucius.biz.po.SubscribeMessage;

import java.util.List;

/**
 * Created by 三十文 on 2017/9/26
 */
public interface AutoMessageService {
    /**
     * 获取自动回复的所有文字信息
     */
    List<AutoReplyMessage> loadTextAutoReplyMessage();

    /**
     * 新增自动回复记录，并将新增的记录返回
     */
    AutoReplyMessage insertAutoReplyMessage(AutoReplyMessage autoReplyMessage);

    /**
     * 更新一条记录，并将更新结果返回
     */
    AutoReplyMessage updateAutoReplyMessage(AutoReplyMessage autoReplyMessage);

    /**
     * 根据 Id 删除自动回复配置
     */
    boolean deleteAutoReplyMessage(Integer autoReplyMessageId);

    /**
     * 获取关注事件的默认回复消息
     */
    List<SubscribeMessage> loadSubscribeDefaultTextMessages();

    /**
     * 更新关注回复消息
     */
    SubscribeMessage updateSubscribeDefaultTextMessage(SubscribeMessage subscribeMessage);
}
