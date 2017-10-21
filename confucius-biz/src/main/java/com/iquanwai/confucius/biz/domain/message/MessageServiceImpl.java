package com.iquanwai.confucius.biz.domain.message;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.common.message.CustomerMessageLogDao;
import com.iquanwai.confucius.biz.dao.common.message.NotifyMessageDao;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.template.TemplateMessage;
import com.iquanwai.confucius.biz.domain.weixin.message.template.TemplateMessageService;
import com.iquanwai.confucius.biz.po.common.message.NotifyMessage;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 17/2/27.
 *  */
@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private NotifyMessageDao notifyMessageDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private CustomerMessageLogDao customerMessageLogDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String SYSTEM_MESSAGE ="AUTO";
    private static final String SYSTEM_MESSAGE_NAME ="系统消息";

    @Override
    public void logCustomerMessage(String openId, Date publishTime, String comment) {
        // CustomerMessageLog customerMessageLog = new CustomerMessageLog();
        // customerMessageLog.setOpenid(openId);
        // customerMessageLog.setPublishTime(publishTime);
        // customerMessageLog.setComment(comment);
        // customerMessageLogDao.insert(customerMessageLog);
    }

    @Override
    //TODO:改造成消息队列
    public void sendMessage(String message, String toUser, String fromUser, String url) {
        NotifyMessage notifyMessage = new NotifyMessage();
        notifyMessage.setFromUser(fromUser);
        notifyMessage.setToUser(toUser);
        notifyMessage.setMessage(message);
        notifyMessage.setIsRead(false);
        notifyMessage.setOld(false);
        notifyMessage.setSendTime(DateUtils.parseDateTimeToString(new Date()));
        notifyMessage.setUrl(url);

        notifyMessageDao.insert(notifyMessage);
    }

    @Override
    public void sendAlarm(String alarmTitle, String alarmTips, String alarmLevel,String desc, String exception) {
        List<String> alarmList = ConfigUtils.getAlarmList();
        alarmList.forEach(openid -> {
            String key = ConfigUtils.incompleteTaskMsgKey();
            TemplateMessage templateMessage = new TemplateMessage();
            templateMessage.setTouser(openid);

            templateMessage.setTemplate_id(key);
            Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
            templateMessage.setData(data);
            String message = "优先级：" + alarmLevel + "\n" +
                    "错误信息：" + desc + "\n" +
                    "异常信息：" + exception;
            data.put("first", new TemplateMessage.Keyword(alarmTitle + "\n"));
            data.put("keyword1", new TemplateMessage.Keyword(alarmTips));
            data.put("keyword2", new TemplateMessage.Keyword("解决异常"));
            data.put("keyword3", new TemplateMessage.Keyword(DateUtils.parseDateTimeToString(new Date())));
            data.put("remark", new TemplateMessage.Keyword(message));
            templateMessageService.sendMessage(templateMessage);
        });
    }

}
