package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.dao.wx.AutoReplyMessageDao;
import com.iquanwai.confucius.biz.dao.wx.SubscribeMessageDao;
import com.iquanwai.confucius.biz.po.AutoReplyMessage;
import com.iquanwai.confucius.biz.po.SubscribeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by 三十文 on 2017/9/26
 */
@Service
public class AutoMessageServiceImpl implements AutoMessageService {

    @Autowired
    private AutoReplyMessageDao autoReplyMessageDao;
    @Autowired
    private SubscribeMessageDao subscribeMessageDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public List<AutoReplyMessage> loadTextAutoReplyMessage() {
        return autoReplyMessageDao.loadAllTextMessages();
    }

    @Override
    public AutoReplyMessage loadDefaultTextAutoReplyMessage() {
        return autoReplyMessageDao.loadDefaultTextMessage();
    }

    @Override
    public AutoReplyMessage insertAutoReplyMessage(AutoReplyMessage autoReplyMessage) {
        int insertResult = autoReplyMessageDao.insert(autoReplyMessage);
        return insertResult > 0 ? autoReplyMessage : null;
    }

    @Override
    public AutoReplyMessage updateAutoReplyMessage(AutoReplyMessage autoReplyMessage) {
        int updateResult = autoReplyMessageDao.update(autoReplyMessage);
        return updateResult > 0 ? autoReplyMessage : null;
    }

    @Override
    public boolean deleteAutoReplyMessage(Integer autoReplyMessageId) {
        int result = autoReplyMessageDao.delete(autoReplyMessageId);
        return result > 0;
    }

    @Override
    public List<SubscribeMessage> loadSubscribeDefaultTextMessages() {
        return subscribeMessageDao.loadSubscribeDefaultTextMessages();
    }

    @Override
    public SubscribeMessage updateSubscribeDefaultTextMessage(SubscribeMessage subscribeMessage) {
        int result = subscribeMessageDao.updateSubscribeDefaultTextMessage(subscribeMessage);
        return result > 0 ? subscribeMessage : null;
    }

}
