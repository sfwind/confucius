package com.iquanwai.confucius.biz.domain.message;


/**
 * Created by justin on 17/2/27.
 */
public interface MessageService {
    /**
     * 评论区回复消息
     * @param message 消息
     * @param fromUser 发送者
     * @param toUser 接收者
     * @param url 打开后跳转的链接
     * */
    void sendMessage(String message, String toUser, String fromUser, String url);

    String SYSTEM_MESSAGE ="AUTO";

    void sendAlarm(String alarmTitle, String alarmTips, String alarmLevel, String desc, String exception);
}
