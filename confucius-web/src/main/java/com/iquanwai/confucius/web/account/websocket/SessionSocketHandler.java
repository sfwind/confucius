package com.iquanwai.confucius.web.account.websocket;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.session.SessionManagerService;
import com.iquanwai.confucius.biz.domain.session.SessionManagerServiceImpl;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.QRCodeUtils;
import com.iquanwai.confucius.web.account.controller.AccountController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.socket.*;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.io.IOException;
import java.util.Map;

/**
 * Created by nethunder on 2016/12/19.
 */
public class SessionSocketHandler implements WebSocketHandler{

    private  static final Logger logger = LoggerFactory.getLogger(SessionSocketHandler.class);

    /**
     * 支付二维码的高度
     * */
    private final static int QRCODE_HEIGHT = 200;
    /**
     * 支付二维码的宽度
     *
     */
    private final static int QRCODE_WIDTH = 200;
    private static String mobileLoginUrl = "/mobile/login/check?sign={sign}&t={unix_timestamp}&s={sessionid}";
    private static String mobileUri = "/mobile/login/check";

    @PostConstruct
    public void initHandler(){
        mobileLoginUrl = ConfigUtils.domainName() + mobileLoginUrl;
    }

    @Autowired
    private SessionManagerService sessionService;


    /**
     * 这个map里存放的是SessionId与Socket的对应<br/>
     * 需要保证一个SessionId对应一个Socket<br/>
     * socket存在的时间为一个浏览器打开登录页面到登录成功，或者该页面异常退出<br/>
     * 当多台机子的时候，这个socketMap是在某一台机子上
     * 移动端扫码时调用通知pc端Http接口时必须要调用确定的ip
     */
    private static final Map<String,WebSocketSession> socketSessionMap = Maps.newConcurrentMap();

    /**
     * 记得加入拦截器排除
     *
     */
    public static void log(Object... obj){
        for (Object o : obj) {
            System.out.print(o);
        }
        System.out.print("\n");
    }
    public static void main(String[] args){
        String param = "{data:[1,2,3]}";
        Map<String,Object> test = CommonUtils.jsonToMap(param);
        log(test.get("data").getClass());

    }

    /**
     * 建立链接，将sessionid加入到session缓存里
     * @param session WebSocketSession，socket的会话
     * @throws Exception 这个要捕获吗 TODO 测试一下不捕获和捕获的区别
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            String sessionId = this.getSessionId(session);
            if (sessionId == null) {
                logger.error("error 无法获得sessionid");
                Map<String, Object> errorMap = Maps.newHashMap();
                errorMap.put("type", "ERROR");
                errorMap.put("msg", "该二维码异常，请刷新二维码");
                session.sendMessage(new TextMessage(CommonUtils.mapToJson(errorMap)));
                session.close();
                return;
            }
            WebSocketSession oldSocket = getLoginSocket(sessionId);
            if (oldSocket != null) {
                // 这里保证一个sessionid只能对应一个Socket
                Map<String, Object> errorMap = Maps.newHashMap();
                errorMap.put("type", "ERROR");
                errorMap.put("msg", "您已经打开登录页面，请不要重复打开哦");
                session.sendMessage(new TextMessage(CommonUtils.mapToJson(errorMap)));
                session.close();
                return;
            }
            // 生成二维码
            String picUrl = createLoginCode(sessionId);
            Map<String, Object> map = Maps.newHashMap();
            map.put("picUrl", picUrl);
            map.put("type", "QR_CREATE");
            // 保持websocket,将salt也存起来
            this.keepLoginSocket(session);
            session.sendMessage(new TextMessage(CommonUtils.mapToJson(map)));
        } catch (Exception e){
            Map<String,Object> map = Maps.newHashMap();
            map.put("type","ERROR");
            map.put("msg",e.getLocalizedMessage());
            session.sendMessage(new TextMessage(CommonUtils.mapToJson(map)));
            session.close();
            logger.error("login socket error after handshake", e);
        }
    }



    /**
     * 处理socket
     * @param session SocketSession
     * @param message socket消息
     * @throws Exception
     */
    @Override
    public void handleMessage(WebSocketSession session,WebSocketMessage<?> message) throws Exception {
        try{
            if(message.getPayload().getClass().equals(String.class)){
                Map<String, Object> body = CommonUtils.jsonToMap( message.getPayload().toString());
                String type = body.get("type") == null ? "" : (String) body.get("type");
                switch(type){
                    case "REFRESH_CODE":{
                        // 刷新二维码
                        String sessionId = this.getSessionId(session);
                        Assert.isTrue(isValidSession(sessionId),"该SessionId无效");
                        refreshQRCode(sessionId);
                        break;
                    }
                    default:{
                        logger.error("invalid socket message type:{}", type);
                    }
                }
            }
        } catch (Exception e){
            logger.error("刷新二维码失败:{}",e);
        }
    }

    /**
     * TODO
     * @param session
     * @param exception
     * @throws Exception
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log("error：",exception);
        session.close();
        this.removeSocketSession(session);
    }

    /**
     * websocket断掉之后不应该删掉httpsession
     * @param session
     * @param closeStatus
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log(session.getId(),",",session.isOpen());
        try {
            // 从静态变量里删除
            this.removeSocketSession(session);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }


    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 从WebSocketSession中获取SessionId
     * @param session WebSocketSession
     * @return SessionId <br/> 如果没有取到SessionId则返回null
     */
    private String getSessionId(WebSocketSession session){
        Object sessionId =  session.getAttributes().get("HTTP.SESSION.ID");
        if(sessionId!=null){
            return sessionId.toString();
        } else {
            return null;
        }
    }

    /**
     * 根据sessionId获得SocketSession
     * @param sessionId sessionId
     * @return WebSocketSession
     */
    public static WebSocketSession getLoginSocket(String sessionId){
        if (sessionId != null) {
            return socketSessionMap.get(sessionId);
        } else {
            return null;
        }
    }

    /**
     * 判断sessionId是否可用
     * @param sessionId sessionId
     * @return sessionId是否在缓存中
     */

    public static boolean isValidSession(String sessionId){
        return socketSessionMap.get(sessionId) != null;
    }


    /**
     * 从WebSocket换从中删掉<br/>
     * 如果socket的id和缓存的id不是同一个，则关闭的不是缓存的id
     * TODO 这里存在问题，如果它关闭链接之前故意将cookie删掉，这里会获取不到sessionid<br/>
     * TODO 需要加上一个定时任务，定时的去扫描缓存，将已经关闭的socket缓存移除
     * @param session WebSocketSession
     */
    private void removeSocketSession(WebSocketSession session){
        String sessionId = this.getSessionId(session);
        WebSocketSession oldSocket = getLoginSocket(sessionId);
        if (sessionId != null && oldSocket != null) {
            // 临时变量，用来与老socket做比较
            if (oldSocket.equals(session)) {
                // 如果关掉的这个socket就是老的，则将它从map中移除
                socketSessionMap.remove(sessionId);
            }
        }
    }

    /**
     * 缓存WebSocket
     * @param session sessionId
     */
    private void keepLoginSocket(WebSocketSession session){
        String sessionId = getSessionId(session);
        // 成功取的sessionid
        if (sessionId != null) {
            socketSessionMap.put(sessionId,session);
        }
    }


    /**
     * 更新二维码链接
     * @param sessionId 根据SessionId更新二维码链接
     */
    public  static void refreshQRCode(String sessionId) throws IOException {
        WebSocketSession session = SessionSocketHandler.getLoginSocket(sessionId);
        String picUrl = createLoginCode(sessionId);
        Map<String,Object> map = Maps.newHashMap();
        map.put("picUrl",picUrl);
        map.put("type","QR_CREATE");
        // 保持websocket,将salt也存起来
        session.sendMessage(new TextMessage(CommonUtils.mapToJson(map)));
    }


    /**
     * 根据SessionId生成二维码链接,返回二维码地址
     * @param sessionId SessionId
     * @return 二维码图片的url
     */
    public static String createLoginCode(String sessionId){
        String loginUrl = loginUrl(sessionId);
        String name = sessionId+ CommonUtils.randomString(8)+".jpg";
        String path = "/data/static/images/qrcode/" + name;
        String picUrl = ConfigUtils.resourceDomainName() + "/images/qrcode/" + name;
        logger.error("二维码url:" + loginUrl);
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
    public static String loginUrl(String sessionId){
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
    public static String sign(String sessionId, String unix_timestamp) {
        Map<String,String> map = Maps.newHashMap();
        map.put("sessionid", sessionId);
        map.put("unix_timestamp",unix_timestamp);
        map.put("salt", ConfigUtils.getLoginSalt());
        map.put("uri", mobileUri);
        return CommonUtils.jsSign(map);
    }





}
