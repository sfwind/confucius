package com.iquanwai.confucius.biz.domain.weixin.message.template;

/**
 * Created by justin on 16/8/10.
 */
public interface TemplateMessageService {
    /**
     * 发送非主动推送模板消息
     * @return 返回发送结果
     * */
    boolean sendMessage(TemplateMessage templateMessage);

    /**
     * @param templateMessage 模板消息对象
     * @param forwardlyPush 是否主动推送
     * @param source 跟踪来源的场景值
     */
    boolean sendMessage(TemplateMessage templateMessage, boolean forwardlyPush, String source);

    /**
     * 根据模板库中的编号获取模板真实id
     * @param templateShortId
     * 模板库中模板的编号，有“TM**”和“OPENTMTM**”等形式
     * */
    String getTemplateId(String templateShortId);


    String getTemplateIdByDB(Integer id);


    String SEND_MESSAGE_URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token={access_token}";
}
