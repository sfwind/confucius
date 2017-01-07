package com.iquanwai.confucius.web.account.controller;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.fragmentation.ChallengeSubmitDao;
import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.exception.ErrorConstants;
import com.iquanwai.confucius.biz.po.ClassMember;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.resolver.PCLoginUser;
import com.iquanwai.confucius.resolver.PCLoginUserResolver;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.account.dto.AccountDto;
import com.iquanwai.confucius.web.account.dto.CourseDto;
import com.iquanwai.confucius.web.account.dto.FragmentDto;
import com.iquanwai.confucius.web.account.dto.LoginCheckDto;
import com.iquanwai.confucius.web.account.websocket.SessionSocketHandler;
import com.iquanwai.confucius.web.pc.dto.ChallengeDto;
import com.iquanwai.confucius.web.pc.dto.ProblemDto;
import org.modelmapper.internal.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2016/12/20.
 */
@RestController
@RequestMapping("/account")
public class AccountController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CourseProgressService courseProgressService;
    @Autowired
    private PlanService planService;

    /**
     * mobile扫描二维码结果
     * @param loginCheckDto 登录检查结果
     * @return pc端处理结果 <br/> 返回值格式：{code:200,msg:{type:1}} <br/>
     * 由于是扫码登录，所以登录逻辑只判断是否是我们的用户,具体的权限在拦截器里判断
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
                // 这里登录成功了，需要获取基本信息
                // 获得用户的openid，根据openid查询用户的学号
                List<ClassMember> classMembers = courseProgressService.loadActiveCourse(loginCheckDto.getLoginUser().getOpenId());
                List<ImprovementPlan> plans = planService.loadUserPlans(loginCheckDto.getLoginUser().getOpenId());
                PCLoginUser pcLoginUser = new PCLoginUser();
                pcLoginUser.setWeixin(loginCheckDto.getLoginUser());
                pcLoginUser.setOpenId(loginCheckDto.getLoginUser().getOpenId());
                // 下面的数据返回前端
                AccountDto accountDto = new AccountDto();
                accountDto.setWeixin(loginCheckDto.getLoginUser());
                accountDto.setOpenId(loginCheckDto.getLoginUser().getOpenId());
                if(classMembers.isEmpty() && plans.isEmpty()){
                    pcLoginUser.setRole("stranger");
                    accountDto.setRole("stranger");
                    // 没有正在就读的班级
                    this.handlerLoginSocket(sessionId,LoginType.PERMISSION_DENIED,accountDto);
                    return WebUtils.success();
                } else {
                    // 缓存起来
                    pcLoginUser.setRole("student");
                    accountDto.setRole("student");
                    // 只查询用户信息
                    // 查询用户的碎片化课程信息
//                    accountDto.setCourse(loadStudentCourse(loginCheckDto.getLoginUser().getOpenId()));
                    PCLoginUserResolver.login(sessionId,pcLoginUser);
                    this.handlerLoginSocket(sessionId,LoginType.LOGIN_SUCCESS,accountDto);
                    return WebUtils.success();
                }
            } else {
                // 校验失败,超时的话刷新二维码
                // TODO 就算校验失败，也应该是能拿到用户信息的
                if (ErrorConstants.SESSION_TIME_OUT==error) {
                    // 扫二维码超时，通知socket更新
                    logger.error("刷新二维码,异常码:{}",error);
                    SessionSocketHandler.refreshQRCode(sessionId);
                    return WebUtils.success();
                } else {
                    // 非超时，单纯校验失败,不做处理
                    logger.error("异常的校验信息,异常码:{}",error);
                    return WebUtils.success();
                }
            }
        } catch (Exception e){
            logger.error("pc登陆结果处理error",e);
            return WebUtils.error(e.getLocalizedMessage());
        }
    }

    @RequestMapping("/get")
    public ResponseEntity<Map<String,Object>> getAccount(PCLoginUser pcLoginUser){
        AccountDto accountDto = new AccountDto();
        accountDto.setWeixin(pcLoginUser.getWeixin());
        accountDto.setOpenId(pcLoginUser.getOpenId());
        accountDto.setRole("student");
        return WebUtils.result(accountDto);
    }

    /**
     * 处理登录信息
     * @param sessionId sessionId
     * @param type 处理结果
     * @param data 发送到前端的数据
     * @throws IOException IO异常信息
     */
    private void handlerLoginSocket(String sessionId,String type, Object data) throws IOException {
        WebSocketSession session = SessionSocketHandler.getLoginSocket(sessionId);
        Map<String,Object> map = Maps.newHashMap();
        map.put("type",type);
        map.put("data",data);
        session.sendMessage(new TextMessage(CommonUtils.mapToJson(map)));
    }

    /**
     * 登录处理结果
     */
    interface LoginType{
        String LOGIN_SUCCESS = "LOGIN_SUCCESS";
        String PERMISSION_DENIED = "PERMISSION_DENIED";

    }
}

