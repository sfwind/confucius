package com.iquanwai.confucius.web.internal.controller;

import com.iquanwai.confucius.biz.domain.message.SMSSendResult;
import com.iquanwai.confucius.biz.domain.message.ShortMessage;
import com.iquanwai.confucius.biz.domain.message.ShortMessageService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.web.internal.dto.SMSDto;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    private OAuthService oAuthService;
    @Autowired
    private AccountService accountService;

    /**
     * 短信发送
     * left:-1:一分钟规则不满足 -2:一小时规则不满足 -3:一天规则不满足 -201:profileId异常 -202:电话号码数量异常 -203:内容异常 -221:请求异常 <br/>
     * right: 当前规则下已经发送多少条了/最大电话数量／最大内容数量/请求结果
     */
    @RequestMapping(value = "/sms/send", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> sendMessage(HttpServletRequest request, @RequestBody SMSDto smsDto) {
        Assert.notNull(smsDto);

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
    }

    /**
     * 用户信息弥补，对于只存在 callback，却没有存储 Profile 和 FollowUser 用户的人员，调用该内部方法若用户不存在会初始化用户信息
     * @param state callback 的随机 state
     */
    @RequestMapping(value = "/init/user", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> initProfile(HttpServletRequest request, @RequestParam("state") String state) {
        Callback callback = oAuthService.getCallbackByState(state);
        Profile profile = accountService.getProfileByUnionId(callback.getUnionId());
        if (profile != null) {
            return WebUtils.success();
        } else {
            return WebUtils.error("刷新用户 Profile 对象失败，state：" + state);
        }
    }

}
