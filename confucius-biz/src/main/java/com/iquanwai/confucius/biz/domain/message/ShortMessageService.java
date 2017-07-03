package com.iquanwai.confucius.biz.domain.message;


import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by nethunder on 2017/6/14.
 * 短信
 */
public interface ShortMessageService {
    String SMS_SEND_URL = "http://www.dh3t.com/json/sms/Submit";
    Integer MAX_PHONE_COUNT = 500;
    Integer MAX_CONTENT_SIZE = 350;

    /**
     * 检查发送限制
     * @param shortMessage 短信
     * @return left:-1:一分钟规则不满足  -2:一小时规则不满足  -3:一天规则不满足 -201:profileId异常 -202:电话号码数量异常 -203:内容异常<br/>
     *         right: 当前规则下已经发送多少条了/最大电话数量／最大内容数量
     */
    Pair<Integer,String> checkSendAble(ShortMessage shortMessage);


    SMSSendResult sendMessage(ShortMessage shortMessage);

    void raiseSendCount(Integer profileId);

    void SMSAlarm(String profileId, String nickname,String msgId, String result, String desc);
}
