package com.iquanwai.confucius.biz.domain.message;


import java.util.Date;

/**
 * Created by justin on 17/2/27.
 */
public interface MessageService {

    /**
     * 记录发送模版消息的情况
     * @param openId openid
     * @param publishTime 发送时间
     * @param comment 备注
     */
    void logCustomerMessage(String openId, Date publishTime, String comment);

    /**
     * 评论区回复消息
     * @param message 消息
     * @param fromUser 发送者
     * @param toUser 接收者
     * @param url 打开后跳转的链接
     * */
    void sendMessage(String message, String toUser, String fromUser, String url);

    String SYSTEM_MESSAGE ="AUTO";

    /**
     * 发送报警消息
     * @param alarmTitle 报警标题
     * @param alarmTips 报警信息
     * @param alarmLevel 报警级别
     * @param desc 描述
     * @param exception 异常信息
     */
    void sendAlarm(String alarmTitle, String alarmTips, String alarmLevel, String desc, String exception);
}
