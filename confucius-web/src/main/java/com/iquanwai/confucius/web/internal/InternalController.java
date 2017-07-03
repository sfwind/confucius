package com.iquanwai.confucius.web.internal;

import com.iquanwai.confucius.biz.domain.message.SMSSendResult;
import com.iquanwai.confucius.biz.domain.message.ShortMessage;
import com.iquanwai.confucius.biz.domain.message.ShortMessageService;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.web.account.dto.SMSDto;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by nethunder on 2017/6/16.
 */
@RestController
@RequestMapping("/internal")
public class InternalController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private ShortMessageService shortMessageService;

    /**
     * 短信发送
     * left:-1:一分钟规则不满足 -2:一小时规则不满足 -3:一天规则不满足 -201:profileId异常 -202:电话号码数量异常 -203:内容异常 -221:请求异常 <br/>
     * right: 当前规则下已经发送多少条了/最大电话数量／最大内容数量/请求结果
     */
    @RequestMapping(value = "/sms/send", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> sendMessage(HttpServletRequest request, @RequestBody SMSDto smsDto) {
        logger.info("param:{}", smsDto);
        Assert.notNull(smsDto);
        String remoteIp = request.getHeader("X-Forwarded-For");
        boolean isInternalIp = CommonUtils.internalIp(remoteIp);
        logger.info("请求来源：{},是否内网：{}", remoteIp, isInternalIp);
        if (isInternalIp) {
            // 是内网ip的请求
            ShortMessage shortMessage = new ShortMessage();
            shortMessage.setProfileId(smsDto.getProfileId());
            shortMessage.setContent(smsDto.getContent());
            shortMessage.setPhone(smsDto.getPhone());
            shortMessage.setType(smsDto.getType());

            // 检查发送条数限制
            Pair<Integer, String> checkSendLimit = shortMessageService.checkSendAble(shortMessage);
            if (checkSendLimit.getLeft() < 0) {
                logger.error("发送参数异常，无法发送{}:{}", checkSendLimit.getLeft(), checkSendLimit.getRight());
                // 不可以发送
                SMSSendResult temp = new SMSSendResult();
                temp.setResult(checkSendLimit.getLeft().toString());
                temp.setDesc(checkSendLimit.getRight());
                return WebUtils.error(temp);
            }

            SMSSendResult result = shortMessageService.sendMessage(shortMessage);
            if (result != null && "0".equals(result.getResult()) && StringUtils.isBlank(result.getFailPhones())) {
                // 提交成功，并且没有发送失败的短信
                shortMessageService.raiseSendCount(smsDto.getProfileId());
                return WebUtils.result(result);
            } else {
                if (result != null && "0".equals(result.getResult()) && !StringUtils.isBlank(result.getFailPhones())) {
                    result.setDesc("发送失败，手机号码异常");
                    return WebUtils.error(result);
                } else {
                    return WebUtils.error(result);
                }
            }
        } else {
            SMSSendResult smsSendResult = new SMSSendResult();
            smsSendResult.setResult("-1");
            smsSendResult.setDesc("非内网请求，禁止访问");
            return WebUtils.error(smsSendResult);
        }
    }
}
