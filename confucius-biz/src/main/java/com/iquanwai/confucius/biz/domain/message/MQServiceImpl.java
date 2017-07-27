package com.iquanwai.confucius.biz.domain.message;

import com.iquanwai.confucius.biz.dao.common.message.MQDealLogDao;
import com.iquanwai.confucius.biz.dao.common.message.MQSendLogDao;
import com.iquanwai.confucius.biz.dao.common.message.MessageQueueDao;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by nethunder on 2017/7/22.
 */
@Service
public class MQServiceImpl implements MQService {
    @Autowired
    private MessageQueueDao messageQueueDao;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MQSendLogDao mqSendLogDao;
    @Autowired
    private MQDealLogDao mqDealLogDao;


    @Override
    public void saveMQSendOperation(MQSendLog mqSendLog){
        // 插入mqSendOperation
        new Thread(() -> {
            String ip = null;
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                ip = localHost.getHostAddress();
            } catch (UnknownHostException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
            mqSendLog.setPublisherIp(ip);
            mqSendLogDao.insert(mqSendLog);
        }).start();
    }

    @Override
    public void updateAfterDealOperation(RabbitMQDto dto) {
        new Thread(() -> {
            String msgId = dto.getMsgId();
            String ip = null;
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                ip = localHost.getHostAddress();
            } catch (UnknownHostException e) {
                logger.error(e.getLocalizedMessage(), e);
            }

            MQDealLog mqDealLog = new MQDealLog();
            mqDealLog.setMsgId(msgId);
            mqDealLog.setTopic(dto.getTopic());
            mqDealLog.setQueue(dto.getQueue());
            mqDealLog.setConsumerIp(ip);
            mqDealLogDao.insert(mqDealLog);
        }).start();
    }

}
