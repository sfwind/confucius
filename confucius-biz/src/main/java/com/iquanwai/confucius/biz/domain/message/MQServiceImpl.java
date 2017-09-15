package com.iquanwai.confucius.biz.domain.message;

import com.iquanwai.confucius.biz.dao.common.message.MQDealLogDao;
import com.iquanwai.confucius.biz.dao.common.message.MQSendLogDao;
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
    private MQSendLogDao mqSendLogDao;
    @Autowired
    private MQDealLogDao mqDealLogDao;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String ipAddress;

    @Override
    public void saveMQSendOperation(MQSendLog mqSendLog) {
        // 插入mqSendOperation
        if (ipAddress == null) {
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                ipAddress = localHost.getHostAddress();
            } catch (UnknownHostException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }

        mqSendLog.setPublisherIp(ipAddress);
        mqSendLogDao.insert(mqSendLog);
    }


    @Override
    public void updateAfterDealOperation(RabbitMQDto dto) {
        String msgId = dto.getMsgId();
        if (ipAddress == null) {
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                ipAddress = localHost.getHostAddress();
            } catch (UnknownHostException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        MQDealLog mqDealLog = new MQDealLog();
        mqDealLog.setMsgId(msgId);
        mqDealLog.setTopic(dto.getTopic());
        mqDealLog.setQueue(dto.getQueue());
        mqDealLog.setConsumerIp(ipAddress);
        mqDealLogDao.insert(mqDealLog);
    }

}
