package com.iquanwai.confucius.web.course.controller;

import com.iquanwai.confucius.biz.domain.course.progress.CourseStudyService;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.*;
import com.iquanwai.confucius.biz.util.ErrorMessageUtils;
import com.iquanwai.confucius.resolver.LoginUser;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.course.dto.EntryDto;
import com.iquanwai.confucius.web.course.dto.InfoSubmitDto;
import com.iquanwai.confucius.web.course.dto.SignupDto;
import org.apache.commons.lang3.tuple.Pair;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by justin on 16/9/10.
 */
@RestController
@RequestMapping("/signup")
public class SignupController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private SignupService signupService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CourseStudyService courseStudyService;

    @RequestMapping(value = "/course/{courseId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> signup(LoginUser loginUser, @PathVariable Integer courseId){
        SignupDto signupDto = new SignupDto();
        String productId = "";
        try{
            Assert.notNull(loginUser, "用户不能为空");
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("报名")
                    .function("课程报名")
                    .action("进入报名页")
                    .memo(courseId+"");
            operationLogService.log(operationLog);
            //课程免单用户
            if (signupService.free(courseId, loginUser.getOpenId())) {
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
            //quanwaiClass.setWeixinGroup(null);
            signupDto.setQuanwaiClass(quanwaiClass);
            signupDto.setRemaining(result.getLeft());
            signupDto.setCourse(signupService.getCachedCourse(courseId));
            productId = signupService.signup(loginUser.getOpenId(), courseId, result.getRight());
            signupDto.setProductId(productId);
            String qrcode = signupService.payQRCode(productId);
            signupDto.setQrcode(qrcode);
        }catch (Exception e){
            //异常关闭订单
            signupService.giveupSignup(loginUser.getOpenId(), productId);
            LOGGER.error("报名失败", e);
            return WebUtils.error("报名人数已满");
        }
        return WebUtils.result(signupDto);
    }

    @RequestMapping(value = "/paid/{orderId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> paid(LoginUser loginUser, @PathVariable String orderId){
        try{
            Assert.notNull(loginUser, "用户不能为空");
            CourseOrder courseOrder = signupService.getCourseOrder(orderId);
            if(courseOrder==null){
                LOGGER.error("{} 订单不存在", orderId);
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.fail"));
            }
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("报名")
                    .function("付费完成")
                    .action("点击付费完成")
                    .memo(orderId);
            operationLogService.log(operationLog);
            if(courseOrder.getStatus()!=1){
                LOGGER.error("订单状态：{}", courseOrder.getStatus());
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.nopaid"));
            }
//            signupService.entry(courseOrder.getCourseId(), courseOrder.getClassId(), courseOrder.getOpenid());
        }catch (Exception e){
            LOGGER.error("支付校验失败", e);
            return WebUtils.error("报名失败");
        }
        return WebUtils.success();
    }

    @RequestMapping(value = "/info/load", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadInfo(LoginUser loginUser){
        InfoSubmitDto infoSubmitDto = new InfoSubmitDto();
        try{
            Assert.notNull(loginUser, "用户不能为空");
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("报名")
                    .function("提交个人信息")
                    .action("加载个人信息");
            operationLogService.log(operationLog);
            Account account = accountService.getAccount(loginUser.getOpenId(), false);
            ModelMapper mapper = new ModelMapper();
            mapper.map(account, infoSubmitDto);
        }catch (Exception e){
            LOGGER.error("加载个人信息失败", e);
            return WebUtils.error("加载个人信息失败");
        }
        return WebUtils.result(infoSubmitDto);
    }

    @RequestMapping(value = "/info/submit", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> infoSubmit(@RequestBody InfoSubmitDto infoSubmitDto,
                                                          LoginUser loginUser){
        Integer chapterId = null;
        try{
            Assert.notNull(loginUser, "用户不能为空");
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("报名")
                    .function("提交个人信息")
                    .action("提交个人信息");
            operationLogService.log(operationLog);
            Account account = new Account();
            ModelMapper mapper = new ModelMapper();
            mapper.map(infoSubmitDto, account);
            account.setOpenid(loginUser.getOpenId());
            accountService.submitPersonalInfo(account);

            Chapter chapter = courseStudyService.loadFirstPreparedChapter(infoSubmitDto.getCourseId());
            if(chapter!=null) {
                chapterId = chapter.getId();
            }
        }catch (Exception e){
            LOGGER.error("提交个人信息失败", e);
            return WebUtils.error("提交个人信息失败");
        }
        return WebUtils.result(chapterId);
    }

    @RequestMapping("/welcome/{orderId}")
    public ResponseEntity<Map<String, Object>> welcome(LoginUser loginUser, @PathVariable String orderId){
        EntryDto entryDto = new EntryDto();
        try{
            Assert.notNull(loginUser, "用户不能为空");
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("报名")
                    .function("报名成功页面")
                    .action("打开报名成功页面")
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
            ClassMember classMember = signupService.classMember(courseOrder.getOpenid(), courseOrder.getClassId());
            if(classMember==null || classMember.getMemberId()==null){
                LOGGER.error("{} 尚未报班 {}", courseOrder.getOpenid(), courseOrder.getClassId());
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.fail"));
            }
            entryDto.setMemberId(classMember.getMemberId());
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
            LOGGER.error("报名成功页面加载失败", e);
            return WebUtils.error("报名失败");
        }
        return WebUtils.result(entryDto);
    }
}
