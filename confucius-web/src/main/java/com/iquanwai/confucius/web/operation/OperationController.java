package com.iquanwai.confucius.web.operation;

import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.message.SMSSendResult;
import com.iquanwai.confucius.biz.domain.message.ShortMessage;
import com.iquanwai.confucius.biz.domain.message.ShortMessageService;
import com.iquanwai.confucius.biz.domain.operation.OperationService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.web.account.dto.SMSDto;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
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

import java.util.Map;

/**
 * Created by xfduan on 2017/5/20.
 */
@RestController
@RequestMapping(value = "/operation")
public class OperationController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private OperationService operationService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private ShortMessageService shortMessageService;

    @RequestMapping(value = "discount", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getDiscountValue(LoginUser loginUser) {
        // 日志记录
//        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("临时活动").function("获取折扣金额");
//        operationLogService.log(operationLog);
//        Integer validaCourseCount = operationService.getValidCourseCount(loginUser.getId());
//        if(validaCourseCount == 0) {
//            return WebUtils.error(202, "无活动权限");
//        }
//        Coupon alreadyGetDiscountCoupon = operationService.alreadyGetDiscount(loginUser.getId());
//        if(alreadyGetDiscountCoupon != null) {
//            if(alreadyGetDiscountCoupon.getExpiredDate().toString().equals(DateUtils.parseDateToString(ConfigUtils.getDiscountExpiredDate()))) {
//                return WebUtils.error(201, alreadyGetDiscountCoupon.getAmount());
//            } else {
//                return WebUtils.result(alreadyGetDiscountCoupon.getAmount());
//            }
//        } else {
//            Integer disCountValue = operationService.getDiscountValue(loginUser.getId());
//            return WebUtils.result(disCountValue);
//        }
        return WebUtils.error(202, "无活动权限");
    }

    @RequestMapping(value = "discount/valid", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> validDiscount(LoginUser loginUser) {
        // 日志记录
//        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("临时活动").function("生效折扣金额");
//        operationLogService.log(operationLog);
//        Integer validCount = operationService.validDiscount(loginUser.getOpenId());
//        if (validCount > 0) {
//            return WebUtils.success();
//        } else {
//            return WebUtils.error("系统错误");
//        }
        return WebUtils.success();
    }

    @RequestMapping(value = "send/message", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> sendMessage(PCLoginUser loginUser, @RequestBody SMSDto smsDto){
        Assert.notNull(loginUser);
        OperationLog operationLog = OperationLog.create()
                .module("运营")
                .function("短信")
                .action("发送短信")
                .memo(loginUser.getProfileId() + "");
        ShortMessage shortMessage = new ShortMessage(loginUser.getProfileId(),
                smsDto.getPhones(), smsDto.getContent(), smsDto.getReplace());
        Pair<Integer, Integer> checkSendAble = shortMessageService.checkSendAble(shortMessage);
        if (checkSendAble.getLeft() > 0) {
            SMSSendResult smsSendResult = shortMessageService.sendMessage(shortMessage);
            if (smsSendResult != null && "0".equals(smsSendResult.getResult())) {
                // 发送成功
                shortMessageService.raiseSendCount(loginUser.getProfileId());
            } else {
                logger.info("发送失败:{}", smsDto);
                return WebUtils.error("发送失败" + (smsSendResult != null ? (":" + smsSendResult.getDesc()) : ""));
            }
            return WebUtils.success();
        } else {
            logger.info("用户:{}，暂时不能发送:{}", loginUser.getProfileId(), checkSendAble);
            return WebUtils.error("暂时不能发送，请稍后再发");
        }
    }
}
