package com.iquanwai.confucius.web.account.controller;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.session.SessionManagerService;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.ErrorMessageUtils;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.account.dto.LoginCheckDto;
import com.iquanwai.confucius.web.account.websocket.SessionSocketHandler;
import org.modelmapper.internal.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;

/**
 * Created by nethunder on 2016/12/20.
 */
@RestController
@RequestMapping("/account")
public class AccountController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SessionManagerService sessionService;

    /**
     * mobile扫描二维码结果
     * @param loginCheckDto 登录检查结果
     * @return pc端处理结果 <br/> 返回值格式：{code:200,msg:{type:1}} <br/>
     * code：200 处理成功<br/>
     * type:1-登录,2-刷新二维码<br/>
     * code:!200 处理失败<br/>
     * msg:异常消息<br/>
     **/
    @RequestMapping(value = "/login/result/", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> loginResult(@RequestBody LoginCheckDto loginCheckDto) {
        try {
            String sessionId = loginCheckDto.getSessionId();
            Integer error = loginCheckDto.getError();
            String status = loginCheckDto.getStatus();
            // 移动端回传check结果
            Assert.notNull(status, "校验状态不能为空");
            Assert.notNull(sessionId, "SessionId不能为空");
            Assert.isTrue(SessionSocketHandler.isValidSession(sessionId),"该SessionId无效");
            // 判断sessionId是否有效
            if (status.equals(Constants.Status.OK)) {
                Assert.notNull(loginCheckDto.getLoginUser(),"用户信息不能为空");
                //校验成功
                this.loginSuccess(sessionId,loginCheckDto.getLoginUser());
                return WebUtils.success();
            } else {
                // 校验失败,超时的话刷新二维码
                // TODO 就算校验失败，也应该是能拿到用户信息的
                if (Constants.AccountError.TIME_OUT.equals(error)) {
                    // 扫二维码超时，通知socket更新
                    SessionSocketHandler.refreshQRCode(sessionId);
                    return WebUtils.success();
                } else {
                    // 非超时，单纯校验失败,不做处理
                    logger.error("异常的校验信息");
                    return WebUtils.success();
                }
            }
        } catch (Exception e){
            logger.error("error",e);
            return WebUtils.error(e.getLocalizedMessage());
        }
    }


    /**
     *  登录成功
     * @param sessionId sessionId
     * @param data 需要发送到页面的数据
     * @throws IOException
     */
    private void loginSuccess(String sessionId, Object data) throws IOException {
        WebSocketSession session = SessionSocketHandler.getLoginSocket(sessionId);
        Map<String,Object> map = Maps.newHashMap();
        map.put("type","LOGIN_SUCCESS");
        map.put("data",data);
        session.sendMessage(new TextMessage(CommonUtils.mapToJson(map)));
        // 登录成功后链接由谁来关闭？ TODO
    }

}

