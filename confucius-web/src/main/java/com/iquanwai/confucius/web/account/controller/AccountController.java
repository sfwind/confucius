package com.iquanwai.confucius.web.account.controller;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.permission.PermissionService;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.exception.ErrorConstants;
import com.iquanwai.confucius.biz.po.common.permisson.Role;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.systematism.ClassMember;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.web.account.dto.AccountDto;
import com.iquanwai.confucius.web.account.dto.LoginCheckDto;
import com.iquanwai.confucius.web.account.websocket.LoginEndpoint;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.resolver.PCLoginUserResolver;
import com.iquanwai.confucius.web.util.CookieUtils;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private PermissionService permissionService;

    /**
     * mobile扫描二维码结果
     *
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
            Assert.isTrue(LoginEndpoint.isValidSession(sessionId), "该SessionId无效");
            // 判断sessionId是否有效
            if (status.equals(Constants.Status.OK)) {
                LoginUser loginUser = loginCheckDto.getLoginUser();
                Assert.notNull(loginUser, "用户信息不能为空");
                // 下面的数据返回前端
                AccountDto accountDto = new AccountDto();
                accountDto.setHeadimgUrl(loginUser.getHeadimgUrl());
                accountDto.setWeixinName(loginUser.getWeixinName());

                Role role = getRole(loginUser.getOpenId());
                accountDto.setRole(role.getLevel());
                accountDto.setKey(sessionId);
                if (role.getLevel().equals(Role.STRANGE)) {
                    // 没有正在就读的班级
                    this.handlerLoginSocket(sessionId, LoginType.PERMISSION_DENIED, accountDto);
                    return WebUtils.error("您还未报名课程，关注圈外了解更多!");
                } else {
                    // 缓存起来
                    PCLoginUser pcLoginUser = new PCLoginUser();
                    pcLoginUser.setWeixin(loginUser);
                    pcLoginUser.setOpenId(loginUser.getOpenId());
                    pcLoginUser.setRole(role.getLevel());
                    pcLoginUser.setPermissionList(permissionService.loadPermissions(role.getLevel()));

                    PCLoginUserResolver.login(sessionId, pcLoginUser);
                    logger.info("{}登录成功,roleId:{},roleLevel:{},key:{}", loginUser.getWeixinName(), role.getId(), role.getLevel(), sessionId);
                    this.handlerLoginSocket(sessionId, LoginType.LOGIN_SUCCESS, accountDto);
                    return WebUtils.success();
                }
            } else {
                // 校验失败,超时的话刷新二维码
                // TODO 就算校验失败，也应该是能拿到用户信息的
                if (ErrorConstants.SESSION_TIME_OUT == error) {
                    // 扫二维码超时，通知socket更新
                    logger.error("刷新二维码,异常码:{}", error);
                    LoginEndpoint.refreshQRCode(sessionId);
                    return WebUtils.success();
                } else if (ErrorConstants.NOT_FOLLOW == error) {
                    logger.error("未关注公众号,异常码:{}",error);
                    LoginEndpoint.jumpServerCode(sessionId);
                    return WebUtils.success();
                } else {
                    // 非超时，单纯校验失败,不做处理
                    logger.error("异常的校验信息,异常码:{}", error);
                    return WebUtils.success();
                }
            }
        } catch (Exception e) {
            logger.error("pc登陆结果处理error", e);
            return WebUtils.error(e.getLocalizedMessage());
        }
    }

    @RequestMapping( value = "/login",method = RequestMethod.GET)
    public void login(HttpServletRequest request, HttpServletResponse response,@ModelAttribute AccountDto accountDto){
        Assert.notNull(accountDto);
        String key = accountDto.getKey();
        if (key != null) {
            if (PCLoginUserResolver.isLogin(key)) {
                CookieUtils.addCookie(LoginEndpoint.QUANWAI_TOKEN_COOKIE_NAME,
                        key, OAuthService.SEVEN_DAYS, response);
            }
        }
        try {
            response.sendRedirect(accountDto.getCallbackUrl());
        } catch (IOException e) {
            logger.error("重定向失败,{}",accountDto);
        }

    }

    private Role getRole(String openid) {
        Role role = permissionService.getRole(openid);
        if(role!=null){
            return role;
        }

        // 获得用户的openid，根据openid查询用户的学号
        List<ClassMember> classMembers = courseProgressService.loadActiveCourse(openid);
        List<ImprovementPlan> plans = planService.loadUserPlans(openid);
        //如果报名了训练营或者开启了RISE,返回学生角色,反之返回陌生人
        if (classMembers.isEmpty() && plans.isEmpty()){
            return Role.stranger();
        }else {
            return Role.student();
        }
    }


    @RequestMapping("/get")
    public ResponseEntity<Map<String, Object>> getAccount(PCLoginUser pcLoginUser) {
        try{
            Assert.notNull(pcLoginUser,"用户不能为空");
            AccountDto accountDto = new AccountDto();
            accountDto.setWeixinName(pcLoginUser.getWeixin().getWeixinName());
            accountDto.setHeadimgUrl(pcLoginUser.getWeixin().getHeadimgUrl());
            accountDto.setRole(pcLoginUser.getRole());
            return WebUtils.result(accountDto);
        } catch (Exception err){
            logger.error("获取用户信息失败",err.getLocalizedMessage());
            return WebUtils.error("获取用户信息失败");
        }
    }

    /**
     * 处理登录信息
     *
     * @param sessionId sessionId
     * @param type      处理结果
     * @param data      发送到前端的数据
     * @throws IOException IO异常信息
     */
    private void handlerLoginSocket(String sessionId, String type, Object data) throws IOException {
        Map<String, Object> map = Maps.newHashMap();
        map.put("type", type);
        map.put("data", data);
        LoginEndpoint.sendMessage(sessionId,CommonUtils.mapToJson(map));
    }

    /**
     * 登录处理结果
     */
    interface LoginType {
        String LOGIN_SUCCESS = "LOGIN_SUCCESS";
        String PERMISSION_DENIED = "PERMISSION_DENIED";

    }
}

