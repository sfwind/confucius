package com.iquanwai.confucius.web.account.websocket;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.QRCodeUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.awt.*;
import java.io.IOException;
import java.util.Map;


/**
 * Created by nethunder on 2017/3/5.
 */
@ServerEndpoint("/session")
public class LoginEndpoint {
    private static Map<String, Session> sessionMap = Maps.newConcurrentMap();
    private static Logger logger = LoggerFactory.getLogger(LoginEndpoint.class);
    private String key;
    /**
     * 支付二维码的高度
     * */
    private final static int QRCODE_HEIGHT = 200;
    /**
     * 支付二维码的宽度
     *
     */
    private final static int QRCODE_WIDTH = 200;
    private static String mobileLoginUrl =  ConfigUtils.domainName() + "/mobile/login/check?sign={sign}&t={unix_timestamp}&s={sessionid}";
    private static String mobileUri = "/mobile/login/check";
    public static String QUANWAI_TOKEN_COOKIE_NAME = "_qt";



    public static boolean isValidSession(String key){
        return sessionMap.get(key) != null;
    }


    @OnOpen
    public void onOpen(Session session) {
        logger.info("建立socket:{}", session.getId());
        String key = CommonUtils.randomString(99);
        if(sessionMap.containsKey(key)){
            key = CommonUtils.randomString(99);
        }
        this.key = key;

        logger.info("{},{},open", key, session.getId());
        try {
            // 生成二维码
            String picUrl = createLoginCode(key);
            sessionMap.put(key, session);
            sendMessage(session,LoginMessage.create("QR_CREATE",picUrl).toJson());
        } catch (Exception e){
            sendMessage(session, LoginMessage.create("ERROR", "请刷新页面").toJson());
            logger.error("login socket error after handshake", e);
        }
    }

    @OnClose
    public void onClose(Session session) {
        logger.info("关闭socket:{}", session.getId());
        if(sessionMap.containsKey(key)){
            sessionMap.remove(key);
            logger.error("{},{},close", this.key, session.getId());
        } else {
            logger.error("{},{},not in map", this.key, session.getId());
        }

    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("出错:{},key:{},error:{}", session.getId(),this.key, throwable);
        try {
            sessionMap.remove(this.key);
            session.close();
        } catch (IOException e) {
            logger.error("error", e);
        }
    }

    @OnMessage
    public void process(Session session,String message){
        logger.info("session:{},key:{}, receive message:{}", session.getId(),key, message);
        try {
            Map<String,Object> msgBody = CommonUtils.jsonToMap(message);
            String type = msgBody.getOrDefault("type","null").toString();
            switch (type) {
                case "REFRESH_CODE": {
                    // 刷新二维码
                    refreshQRCode(this.key);
                    break;
                }
                default: {
                    logger.error("invalid socket message type:{}", type);
                }
            }
        } catch (Exception e){
            logger.error("");
        }
    }

    public static void sendMessage(Session session, String message) {
        try {
            RemoteEndpoint.Basic remote = session.getBasicRemote();
            remote.sendText(message);
        } catch (Exception e) {
            logger.error("send异常", e);
        }
    }

    public static void sendMessage(String key, String message) {
        sendMessage(sessionMap.get(key), message);
    }


    /**
     * 根据SessionId生成二维码链接,返回二维码地址
     * @param sessionId SessionId
     * @return 二维码图片的url
     */
    private static String createLoginCode(String sessionId){
        String loginUrl = loginUrl(sessionId);
        String name = sessionId+ CommonUtils.randomString(8)+".jpg";
        String path = "/data/static/images/qrcode/" + name;
        String picUrl = ConfigUtils.domainName() + "/images/qrcode/" + name;
        logger.info("二维码url:" + loginUrl);
        //生成二维码base64编码
        Image image = QRCodeUtils.genQRCode(loginUrl,QRCODE_WIDTH,QRCODE_HEIGHT);
        if(image==null){
            logger.error("二维码生成失败");
        } else {
            QRCodeUtils.image2FS(image, path);
        }
        return picUrl;
    }

    /**
     * 生成二维码URL
     * @param sessionId sessionId
     */
    private static String loginUrl(String sessionId){
        String time_stamp = String.valueOf(DateUtils.currentTimestamp());
        Map<String, String> map = Maps.newHashMap();
        map.put("unix_timestamp", time_stamp);
        map.put("sessionid", sessionId);
        map.put("sign", sign(sessionId, time_stamp));
        return CommonUtils.placeholderReplace(mobileLoginUrl, map);
    }

    /**
     * 签名
     * @param sessionId sessionId
     */
    private static String sign(String sessionId, String unix_timestamp) {
        Map<String,String> map = Maps.newHashMap();
        map.put("sessionid", sessionId);
        map.put("unix_timestamp",unix_timestamp);
        map.put("salt", ConfigUtils.getLoginSalt());
        map.put("uri", mobileUri);
        return CommonUtils.jsSign(map);
    }

    /**
     * 更新二维码链接
     * @param key 根据SessionId更新二维码链接
     */
    public  static void refreshQRCode(String key) throws IOException {
        Session session = sessionMap.get(key);
        if (session == null) {
            logger.error("key:{} not in sessionMap",key);
            return;
        }
        String picUrl = createLoginCode(key);
        sendMessage(session,LoginMessage.create("QR_CREATE",picUrl).toJson());
    }

    public static void jumpServerCode(String key) throws IOException {
        Session session = sessionMap.get(key);
        if (session == null) {
            logger.error("key:{} not in sessionMap",key);
            return;
        }
        sendMessage(session, LoginMessage.create("NOT_FOLLOW", "").toJson());
    }

}

@Data
class LoginMessage{
    private String type;
    private String msg;

    public static LoginMessage create(String type,String message){
        LoginMessage msg = new LoginMessage();
        msg.setType(type);
        msg.setMsg(message);
        return msg;
    }

    public String toJson(){
        return new Gson().toJson(this);
    }
}