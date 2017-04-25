package com.iquanwai.confucius.biz.util.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ConnectException;

/**
 * Created by justin on 17/1/19.
 */
@Service
public class RabbitMQReceiver {
    private String topic;
    private Connection connection;
    private Channel channel;
    private String ipAddress;
    private String queue;
    private int port = 5672;

    public void init(String topic, String queue, String ipAddress, Integer port){
        Assert.notNull(topic, "消息主题不能为空");
        Assert.notNull(ipAddress, "rabbit ip不能为空");
        destroy();
        this.topic = topic;
        this.ipAddress = ipAddress;
        this.queue = queue;
        if (port != null) {
            this.port = port;
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(ipAddress);
        factory.setPort(this.port);
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            //队列声明,广播形式
            channel.exchangeDeclare(topic, "fanout");
            //交换机声明,默认不持久化
            channel.queueDeclare(queue, false, false, false, null);
            //队列交换机绑定
            channel.queueBind(queue, topic, "");
        }catch (IOException e) {
            //ignore
        }
    }

    @PreDestroy
    public void destroy(){
        try {
            if(channel!=null) {
                channel.close();
            }
            if(connection!=null) {
                connection.close();
            }
        }catch (IOException e) {
            //ignore
        }
    }

    public void listen(Consumer consumer) throws ConnectException {
        //重连尝试
        if(connection==null){
            init(topic, queue, ipAddress, port);
        }
        if(channel==null){
            throw new ConnectException();
        }
        try{
            channel.basicConsume(queue, true, consumer);
        }catch (IOException e){
            //ignore
        }
    }

}
