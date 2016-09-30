package com.iquanwai.confucius.web.course.controller;

import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.CourseOrder;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.QuanwaiClass;
import com.iquanwai.confucius.biz.util.ErrorMessageUtils;
import com.iquanwai.confucius.resolver.LoginUser;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.course.dto.EntryDto;
import com.iquanwai.confucius.web.course.dto.SignupDto;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by justin on 16/9/10.
 */
@RestController
@RequestMapping("/signup")
public class SignupController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private SignupService signupService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private AccountService accountService;

    @RequestMapping(value = "/course/{courseId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> signup(LoginUser loginUser, @PathVariable int courseId){
        SignupDto signupDto = new SignupDto();
        try{
            Assert.notNull(loginUser, "用户不能为空");
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("报名")
                    .function("课程报名")
                    .action("进入报名页")
                    .memo(courseId+"");
            operationLogService.log(operationLog);
            //课程免单用户
            if (signupService.isWhite(courseId, loginUser.getOpenId())) {
                signupDto.setFree(true);
                return WebUtils.result(signupDto);
            }
            Pair<Integer, Integer> result = signupService.signupCheck(loginUser.getOpenId(), courseId);

            if(result.getLeft()==-1){
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.full"));
            }
            if(result.getLeft()==-2){
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.noclass"));
            }
            if(result.getLeft()==-3){
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.already"));
            }
            QuanwaiClass quanwaiClass = signupService.getCachedClass(result.getRight());
            //去掉群二维码
            quanwaiClass.setWeixinGroup(null);
            signupDto.setQuanwaiClass(quanwaiClass);
            signupDto.setRemaining(result.getLeft());
            signupDto.setCourse(signupService.getCachedCourse(courseId));
            String productId = signupService.signup(loginUser.getOpenId(), courseId, result.getRight());
            signupDto.setProductId(productId);
            String qrcode = signupService.payQRCode(productId);
            signupDto.setQrcode(qrcode);
        }catch (Exception e){
            LOGGER.error("报名失败", e);
            return WebUtils.error("报名人数已满");
        }
        return WebUtils.result(signupDto);
    }

    @RequestMapping(value = "/paid/{orderId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> paid(LoginUser loginUser, @PathVariable String orderId){
        EntryDto entryDto = new EntryDto();
        try{
            Assert.notNull(loginUser, "用户不能为空");
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("报名")
                    .function("付费完成")
                    .action("点击付费完成")
                    .memo(orderId);
            operationLogService.log(operationLog);
            CourseOrder courseOrder = signupService.getCourseOrder(orderId);
            if(courseOrder==null){
                LOGGER.error("{} 订单不存在", orderId);
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.fail"));
            }
            if(courseOrder.getStatus()!=1){
                LOGGER.error("订单状态：{}", courseOrder.getStatus());
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.nopaid"));
            }

            String memberId = signupService.entry(courseOrder.getCourseId(), courseOrder.getClassId(), courseOrder.getOpenid());
            entryDto.setMemberId(memberId);
            entryDto.setQuanwaiClass(signupService.getCachedClass(courseOrder.getClassId()));
            entryDto.setCourse(signupService.getCachedCourse(courseOrder.getCourseId()));
            Account account = accountService.getAccount(loginUser.getOpenId(), true);
            if(account!=null) {
                entryDto.setUsername(account.getNickname());
                entryDto.setHeadUrl(account.getHeadimgurl());
            }else{
                entryDto.setUsername(loginUser.getWeixinName());
                entryDto.setHeadUrl(loginUser.getHeadimgUrl());
            }
        }catch (Exception e){
            LOGGER.error("报名失败", e);
            return WebUtils.error("报名人数已满");
        }
        return WebUtils.result(entryDto);
    }
}
