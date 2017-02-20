package com.iquanwai.confucius.web.course.controller;

import com.iquanwai.confucius.biz.domain.course.operational.PromoCodeService;
import com.iquanwai.confucius.biz.domain.course.progress.CourseStudyService;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.customer.ProfileService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayService;
import com.iquanwai.confucius.biz.exception.ErrorConstants;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.PromoCode;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.customer.Profile;
import com.iquanwai.confucius.biz.po.systematism.Chapter;
import com.iquanwai.confucius.biz.po.systematism.ClassMember;
import com.iquanwai.confucius.biz.po.systematism.Course;
import com.iquanwai.confucius.biz.po.systematism.CourseIntroduction;
import com.iquanwai.confucius.biz.po.systematism.CourseOrder;
import com.iquanwai.confucius.biz.po.systematism.QuanwaiClass;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.ErrorMessageUtils;
import com.iquanwai.confucius.web.course.dto.EntryDto;
import com.iquanwai.confucius.web.course.dto.InfoSubmitDto;
import com.iquanwai.confucius.web.course.dto.SignupDto;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
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
    @Autowired
    private ProfileService profileService;
    @Autowired
    private PromoCodeService promoCodeService;
    @Autowired
    private PayService payService;

    @RequestMapping(value = "/course/{courseId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> signup(LoginUser loginUser, @PathVariable Integer courseId, HttpServletRequest request){
        SignupDto signupDto = new SignupDto();
        String productId = "";
        try{
            Assert.notNull(loginUser, "用户不能为空");
            String remoteIp = request.getHeader("X-Forwarded-For");
            if(remoteIp==null){
                LOGGER.error("获取用户:{} 获取IP失败:CourseId:{}", loginUser.getOpenId(), courseId);
                remoteIp = ConfigUtils.getExternalIP();
            }
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
            // TODO 检查人数，已加锁。
            Pair<Integer, Integer> result = signupService.signupCheck(loginUser.getOpenId(), courseId);
            if(result.getLeft()==-1){
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.full"));
            }
            if(result.getLeft()==-2){
                return WebUtils.error(ErrorConstants.COURSE_NOT_OPEN,ErrorMessageUtils.getErrmsg("signup.noclass"));
            }
            if(result.getLeft()==-3){
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.already"));
            }
            QuanwaiClass quanwaiClass = signupService.getCachedClass(result.getRight());
            signupDto.setQuanwaiClass(quanwaiClass);
            signupDto.setRemaining(result.getLeft());
            CourseIntroduction courseIntroduction = signupService.getCachedCourse(courseId);
            signupDto.setCourse(courseIntroduction);
            // 计算关闭课程的时间
            if(courseIntroduction.getType() == Course.LONG_COURSE){
                // 长课程
                signupDto.setClassOpenTime(DateUtils.parseDateToStringByCommon(quanwaiClass.getOpenTime()) +
                        " - " + DateUtils.parseDateToStringByCommon(quanwaiClass.getCloseTime()));
            } else if(courseIntroduction.getType() == Course.SHORT_COURSE){
                // 短课程
                signupDto.setClassOpenTime(DateUtils.parseDateToStringByCommon(new Date()) + " - " +
                        DateUtils.parseDateToStringByCommon(DateUtils.afterDays(new Date(), courseIntroduction.getLength()+6)));
            } else if(courseIntroduction.getType() == Course.AUDITION_COURSE) {
                signupDto.setClassOpenTime("7天");
            }

            // TODO 优惠券改为可选，下面这个service放到新接口，增加优惠券参数
            QuanwaiOrder courseOrder = signupService.signup(loginUser.getOpenId(), courseId, result.getRight());
            productId = courseOrder.getOrderId();
            if(courseOrder.getDiscount()!=0.0){
                signupDto.setNormal(courseOrder.getTotal());
                signupDto.setDiscount(courseOrder.getDiscount());
            }
            signupDto.setFee(courseOrder.getPrice());
            signupDto.setProductId(productId);
            //TODO 现在只有一种支付方式，当有多种支付方式时，下面微信支付多种方式为多种接口
//            String qrcode = signupService.payQRCode(productId);
//            signupDto.setQrcode(qrcode);
            //TODO 只有求职课程才使用优惠码
            if(courseId == 2){
                signupDto.setNormal(courseOrder.getTotal());
                PromoCode promoCode = promoCodeService.getPromoCode(loginUser.getOpenId());
                signupDto.setPromoCode(promoCode);
            }

            // 统一下单
            Map<String, String> signParams = payService.buildH5PayParam(productId,remoteIp,loginUser.getOpenId());
            signupDto.setSignParams(signParams);

            OperationLog payParamLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("报名")
                    .function("微信支付")
                    .action("下单")
                    .memo(signParams.toString());
            operationLogService.log(payParamLog);
        }catch (Exception e){
            LOGGER.error("报名失败", e);
            //异常关闭订单
            if(StringUtils.isNotEmpty(productId)) {
                signupService.giveupSignup(productId);
            }
            return WebUtils.error("报名人数已满");
        }
        return WebUtils.result(signupDto);
    }

    @RequestMapping(value = "/check/{productId}/{promoCode}")
    public ResponseEntity<Map<String, Object>> checkCoursePromoCode(LoginUser loginUser,
                                                                    @PathVariable("productId") String productId,
                                                                    @PathVariable("promoCode") String promoCode,
                                                                    HttpServletRequest request) {
        // TODO 优惠券相关，可能删除
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(productId, "单号不能为空");
        Assert.notNull(promoCode, "优惠码不能为空");
        SignupDto signupDto = new SignupDto();
        String remoteIp = request.getHeader("X-Forwarded-For");
        if(remoteIp==null){
            LOGGER.error("获取用户:{} 获取IP失败", loginUser.getOpenId());
            remoteIp = ConfigUtils.getExternalIP();
        }
        // 校验二维码
        Double discount = promoCodeService.discount(promoCode);
        if(discount == -1.0){
            // 优惠券不可用
            return WebUtils.error(ErrorConstants.PROMO_CODE_INVALID,"该优惠码已过期");
        } else {
            CourseOrder order = signupService.getOrder(productId);
            Assert.notNull(order,"订单信息不能为空");
            // 先关掉所有该课程的老订单 TODO 记录，先不关闭
//            List<QuanwaiOrder> activeOrders = signupService.getActiveOrders(loginUser.getOpenId(), order.getCourseId());
//            activeOrders.forEach(item->signupService.giveupSignup(order.getOrderId()));
            // 优惠券可用，重新插入订单
            Pair<Integer, Integer> result = signupService.signupCheck(loginUser.getOpenId(), order.getCourseId());
            if(result.getLeft()==-1){
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.full"));
            }
            if(result.getLeft()==-2){
                return WebUtils.error(ErrorConstants.COURSE_NOT_OPEN,ErrorMessageUtils.getErrmsg("signup.noclass"));
            }
            if(result.getLeft()==-3){
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.already"));
            }
            //去掉群二维码
            //quanwaiClass.setWeixinGroup(null);
            // TODO 优惠券信息与优惠码信息
            QuanwaiOrder courseOrder = signupService.signup(loginUser.getOpenId(), order.getCourseId(), result.getRight(),promoCode,discount);
            String newProductId = courseOrder.getOrderId();
            if(courseOrder.getDiscount() != 0.0){
                signupDto.setNormal(courseOrder.getTotal());
                signupDto.setDiscount(courseOrder.getDiscount());
            }
            signupDto.setFee(courseOrder.getPrice());
            signupDto.setProductId(newProductId);
//            String qrcode = signupService.payQRCode(newProductId);
//            signupDto.setQrcode(qrcode);
            signupService.updatePromoCode(newProductId,promoCode);

            // 统一下单
            Map<String, String> signParams = payService.buildH5PayParam(newProductId,remoteIp,loginUser.getOpenId());
            signupDto.setSignParams(signParams);
            OperationLog payParamLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("报名")
                    .function("微信支付")
                    .action("下单")
                    .memo(signParams.toString());
            operationLogService.log(payParamLog);

            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("报名")
                    .function("推广")
                    .action("验证优惠码")
                    .memo(productId+":"+promoCode);
            operationLogService.log(operationLog);
            return WebUtils.result(signupDto);
        }
    }

    @RequestMapping(value = "/paid/{orderId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> paid(LoginUser loginUser, @PathVariable String orderId){
        Assert.notNull(loginUser, "用户不能为空");
        CourseOrder courseOrder = signupService.getOrder(orderId);
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
        QuanwaiOrder quanwaiOrder = signupService.getQuanwaiOrder(orderId);
        Double zero = 0d;
        if (zero.equals(quanwaiOrder.getPrice())) {
            // 免费，自动报名
            payService.handlePayResult(orderId,true);
            payService.paySuccess(orderId);
            // 支付成功,查看该订单是否使用了 TODO 优惠券相关,可能删除
            if(courseOrder.getPromoCode()!=null){
                LOGGER.info("用户:{},使用优惠券:{}",courseOrder.getOpenid(),courseOrder.getPromoCode());
                promoCodeService.usePromoCode(courseOrder.getOpenid(),courseOrder.getPromoCode());
            }
        } else {
            // 非免费，查询是否报名成功
            if(!courseOrder.getEntry()){
                LOGGER.error("订单:{},未支付", courseOrder.getOrderId());
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.nopaid"));
            }
        }

//        signupService.entry(courseOrder.getCourseId(), courseOrder.getClassId(), courseOrder.getOpenid());
        return WebUtils.success();
    }

    @RequestMapping(value = "/info/load", method = RequestMethod.GET)
    @Deprecated
    public ResponseEntity<Map<String, Object>> loadInfo(LoginUser loginUser){
        InfoSubmitDto infoSubmitDto = new InfoSubmitDto();
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("提交个人信息")
                .action("加载个人信息");
        operationLogService.log(operationLog);
        Profile account = profileService.getProfile(loginUser.getOpenId());
        try {
            BeanUtils.copyProperties(infoSubmitDto, account);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("beanUtils copy props error", e);
            return WebUtils.error("加载个人信息失败");
        }
        return WebUtils.result(infoSubmitDto);
    }

    @RequestMapping(value = "/info/submit", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> infoSubmit(@RequestBody InfoSubmitDto infoSubmitDto,
                                                          LoginUser loginUser){
        Integer chapterId = null;
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("提交个人信息")
                .action("提交个人信息");
        operationLogService.log(operationLog);
        Profile account = new Profile();
        try {
            BeanUtils.copyProperties(account, infoSubmitDto);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("beanUtils copy props error", e);
            return WebUtils.error("提交个人信息失败");
        }
        account.setOpenid(loginUser.getOpenId());
        profileService.submitPersonalInfo(account,false);

        Chapter chapter = courseStudyService.loadFirstChapter(infoSubmitDto.getCourseId());
        if(chapter!=null) {
            chapterId = chapter.getId();
        }
        return WebUtils.result(chapterId);
    }

    @RequestMapping("/welcome/{orderId}")
    public ResponseEntity<Map<String, Object>> welcome(LoginUser loginUser, @PathVariable String orderId){
        EntryDto entryDto = new EntryDto();
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("报名成功页面")
                .action("打开报名成功页面")
                .memo(orderId);
        operationLogService.log(operationLog);
        CourseOrder courseOrder = signupService.getOrder(orderId);
        if(courseOrder==null){
            LOGGER.error("{} 订单不存在", orderId);
            return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.fail"));
        }
        if(courseOrder.getEntry()){
            LOGGER.error("订单{}未支付", courseOrder.getOrderId());
            return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.nopaid"));
        }
        ClassMember classMember = signupService.classMember(orderId);
        if(classMember==null || classMember.getMemberId()==null){
            LOGGER.error("{} 尚未报班", loginUser.getOpenId());
            return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.fail"));
        }
        entryDto.setMemberId(classMember.getMemberId());
        entryDto.setQuanwaiClass(signupService.getCachedClass(classMember.getClassId()));
        entryDto.setCourse(signupService.getCachedCourse(classMember.getCourseId()));
        Account account = accountService.getAccount(loginUser.getOpenId(), true);
        if(account!=null) {
            entryDto.setUsername(account.getNickname());
            entryDto.setHeadUrl(account.getHeadimgurl());
        }else{
            entryDto.setUsername(loginUser.getWeixinName());
            entryDto.setHeadUrl(loginUser.getHeadimgUrl());
        }
        return WebUtils.result(entryDto);
    }
}
