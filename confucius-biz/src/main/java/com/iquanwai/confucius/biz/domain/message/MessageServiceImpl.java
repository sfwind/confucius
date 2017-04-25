package com.iquanwai.confucius.biz.domain.message;

import com.iquanwai.confucius.biz.dao.common.message.NotifyMessageDao;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.common.message.NotifyMessage;
import com.iquanwai.confucius.biz.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by justin on 17/2/27.
 *  */
@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private NotifyMessageDao notifyMessageDao;
    @Autowired
    private AccountService accountService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String SYSTEM_MESSAGE ="AUTO";
    private static final String SYSTEM_MESSAGE_NAME ="系统消息";

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

}
