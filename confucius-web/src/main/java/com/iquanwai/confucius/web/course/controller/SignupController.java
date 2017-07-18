package com.iquanwai.confucius.web.course.controller;

import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.domain.course.progress.CourseStudyService;
import com.iquanwai.confucius.biz.domain.course.signup.CostRepo;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.customer.ProfileService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayService;
import com.iquanwai.confucius.biz.exception.ErrorConstants;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
import com.iquanwai.confucius.biz.po.fragmentation.RiseOrder;
import com.iquanwai.confucius.biz.po.systematism.Chapter;
import com.iquanwai.confucius.biz.po.systematism.ClassMember;
import com.iquanwai.confucius.biz.po.systematism.Course;
import com.iquanwai.confucius.biz.po.systematism.CourseIntroduction;
import com.iquanwai.confucius.biz.po.systematism.CourseOrder;
import com.iquanwai.confucius.biz.po.systematism.QuanwaiClass;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.ErrorMessageUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import com.iquanwai.confucius.web.course.dto.EntryDto;
import com.iquanwai.confucius.web.course.dto.InfoSubmitDto;
import com.iquanwai.confucius.web.course.dto.RiseCourseDto;
import com.iquanwai.confucius.web.course.dto.RiseMemberDto;
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
import java.util.List;
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
    private PayService payService;
    @Autowired
    private CourseProgressService courseProgressService;
    @Autowired
    private CostRepo costRepo;
    @Autowired
    private RestfulHelper restfulHelper;

    @RequestMapping(value = "/course/{courseId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> signup(LoginUser loginUser, @PathVariable Integer courseId, HttpServletRequest request) {
        SignupDto signupDto = new SignupDto();
        String productId = "";
        try {
            Assert.notNull(loginUser, "用户不能为空");
            String remoteIp = request.getHeader("X-Forwarded-For");
            if (remoteIp == null) {
                LOGGER.error("获取用户:{} 获取IP失败:CourseId:{}", loginUser.getOpenId(), courseId);
                remoteIp = ConfigUtils.getExternalIP();
            }
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("报名")
                    .function("课程报名")
                    .action("进入报名页")
                    .memo(courseId + "");
            operationLogService.log(operationLog);

            //课程免单用户
//            if (signupService.free(courseId, loginUser.getOpenId())) {
//                signupDto.setFree(true);
//                return WebUtils.result(signupDto);
//            }
            // 检查人数，已加锁。
            ClassMember classMember = courseProgressService.loadActiveCourse(loginUser.getId(), courseId);
            if (classMember != null) {
                // 已报名
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.already"));
            }

            Pair<Integer, Integer> result = signupService.signupCheck(loginUser.getId(), courseId);
            if (result.getLeft() == -1) {
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.full"));
            }
            if (result.getLeft() == -2) {
                return WebUtils.error(ErrorConstants.COURSE_NOT_OPEN, ErrorMessageUtils.getErrmsg("signup.noclass"));
            }

            QuanwaiClass quanwaiClass = signupService.getCachedClass(result.getRight());
            signupDto.setQuanwaiClass(quanwaiClass);
            signupDto.setRemaining(result.getLeft());
            CourseIntroduction courseIntroduction = signupService.getCachedCourse(courseId);
            signupDto.setCourse(courseIntroduction);
            // 计算关闭课程的时间
            if (courseIntroduction.getType() == Course.LONG_COURSE) {
                // 长课程
                signupDto.setClassOpenTime(DateUtils.parseDateToStringByCommon(quanwaiClass.getOpenTime()) +
                        " - " + DateUtils.parseDateToStringByCommon(quanwaiClass.getCloseTime()));
            } else if (courseIntroduction.getType() == Course.SHORT_COURSE) {
                // 短课程
                signupDto.setClassOpenTime(DateUtils.parseDateToStringByCommon(new Date()) + " - " +
                        DateUtils.parseDateToStringByCommon(DateUtils.afterDays(new Date(), courseIntroduction.getLength() + 6)));
            } else if (courseIntroduction.getType() == Course.AUDITION_COURSE) {
                signupDto.setClassOpenTime("7天");
            }
            // TODO 优惠券改为可选，下面这个service放到新接口，增加优惠券参数
            QuanwaiOrder quanwaiOrder = signupService.signupCourse(loginUser.getOpenId(), loginUser.getId(),
                    courseId, result.getRight());
            productId = quanwaiOrder.getOrderId();
            if (quanwaiOrder.getDiscount() != 0.0) {
                signupDto.setNormal(quanwaiOrder.getTotal());
                signupDto.setDiscount(quanwaiOrder.getDiscount());
            }
            signupDto.setFee(quanwaiOrder.getPrice());
            signupDto.setProductId(productId);
            //TODO 现在只有一种支付方式，当有多种支付方式时，下面微信支付多种方式为多种接口
//            String qrcode = signupService.payQRCode(productId);
//            signupDto.setQrcode(qrcode);

            // 统一下单
            if (quanwaiOrder.getPrice() != null && quanwaiOrder.getPrice() != 0) {
                Map<String, String> signParams = payService.buildH5PayParam(productId, remoteIp, loginUser.getOpenId());
                signupDto.setSignParams(signParams);
                OperationLog payParamLog = OperationLog.create().openid(loginUser.getOpenId())
                        .module("报名")
                        .function("微信支付")
                        .action("下单")
                        .memo(signParams.toString());
                operationLogService.log(payParamLog);
            }
        } catch (Exception e) {
            LOGGER.error("报名失败", e);
            //异常关闭订单
            if (StringUtils.isNotEmpty(productId)) {
                signupService.giveupSignup(productId);
            }
            return WebUtils.error("报名人数已满");
        }
        return WebUtils.result(signupDto);
    }

    /**
//     * Rise创建订单，并未下单
//     *
//     * @param riseMemberDto
//     * @return
//     */
//    @RequestMapping(value = "/rise/signup")
//    public ResponseEntity<Map<String, Object>> riseMemberSignup(LoginUser loginUser, @RequestBody RiseMemberDto riseMemberDto) {
//        Assert.notNull(loginUser, "用户不能为空");
//        Assert.notNull(riseMemberDto, "请求参数不能为空");
//        String openId = riseMemberDto.getOpenId();
//        Integer memberType = riseMemberDto.getMemberType();
//        OperationLog operationLog = OperationLog.create().openid(openId)
//                .module("训练营")
//                .function("RISE报名")
//                .action("创建订单")
//                .memo(memberType + "");
//        operationLogService.log(operationLog);
//        // 在这里加锁
//
//        // 创建订单
//        QuanwaiOrder quanwaiOrder = null;
//        try {
//            quanwaiOrder = signupService.signupRiseMember(loginUser.getId(), memberType, null).getRight();
//        } catch (Exception e) {
//            return WebUtils.error(e.getLocalizedMessage());
//        }
//
//        return WebUtils.result(quanwaiOrder);
//    }

//    @RequestMapping(value = "/info/{productId}")
//    public ResponseEntity<Map<String, Object>> queryProductInfo(LoginUser loginUser, @PathVariable("productId") String productId, HttpServletRequest request) {
//        Assert.notNull(loginUser, "用户不能为空");
//        QuanwaiOrder quanwaiOrder = signupService.getQuanwaiOrder(productId);
//        Assert.notNull(quanwaiOrder, "订单信息不能为空");
//        Assert.notNull(quanwaiOrder.getPrice(), "订单金额不能为空");
//        String remoteIp = request.getHeader("X-Forwarded-For");
//        if (remoteIp == null) {
//            LOGGER.error("获取用户:{} 获取IP失败:quanwaiOrder:{}", loginUser.getOpenId(), quanwaiOrder);
//            remoteIp = ConfigUtils.getExternalIP();
//        }
//        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
//                .module("RISE")
//                .function("会员报名")
//                .action("进入付费页")
//                .memo(productId);
//        operationLogService.log(operationLog);
//        SignupDto signupDto = new SignupDto();
//        if (quanwaiOrder.getPrice() != 0) {
//            // 碎片化统一下单
//            Map<String, String> signParams = payService.buildH5PayParam(productId, remoteIp, loginUser.getOpenId());
//            signupDto.setSignParams(signParams);
//            OperationLog payParamLog = OperationLog.create().openid(loginUser.getOpenId())
//                    .module("报名")
//                    .function("微信支付")
//                    .action("下单")
//                    .memo(signParams.toString());
//            operationLogService.log(payParamLog);
//            if (QuanwaiOrder.FRAGMENT_MEMBER.equals(quanwaiOrder.getGoodsType())) {
//                // 碎片化订单
//                MemberType memberType = signupService.getMemberType(Integer.parseInt(quanwaiOrder.getGoodsId()));
//                signupDto.setMemberType(memberType);
//            } else {
//                signupDto.setFree(true);
//            }
//        } else {
//            // TODO  体系化
//        }
//        signupDto.setGoodsType(quanwaiOrder.getGoodsType());
//        signupDto.setProductId(productId);
//        if (quanwaiOrder.getDiscount() != 0.0) {
//            signupDto.setNormal(quanwaiOrder.getTotal());
//            signupDto.setDiscount(quanwaiOrder.getDiscount());
//        }
//        signupDto.setFee(quanwaiOrder.getPrice());
//        signupDto.setProductId(productId);
//        return WebUtils.result(signupDto);
//    }


    @RequestMapping(value = "/paid/{orderId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> paid(LoginUser loginUser, @PathVariable String orderId) {
        Assert.notNull(loginUser, "用户不能为空");
        CourseOrder courseOrder = signupService.getOrder(orderId);
        if (courseOrder == null) {
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
            payService.handlePayResult(orderId, true);
            payService.paySuccess(orderId);
        } else {
            // 非免费，查询是否报名成功
            if (!courseOrder.getEntry()) {
                LOGGER.error("订单:{},未支付", courseOrder.getOrderId());
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.nopaid"));
            }
        }

//        signupService.entry(courseOrder.getCourseId(), courseOrder.getClassId(), courseOrder.getOpenid());
        return WebUtils.success();
    }

    @RequestMapping(value = "/paid/risemember/{orderId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> riseMemberPaid(LoginUser loginUser, @PathVariable String orderId) {
        Assert.notNull(loginUser, "用户不能为空");
        RiseOrder riseOrder = signupService.getRiseOrder(orderId);
        if (riseOrder == null) {
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
            payService.handlePayResult(orderId, true);
            payService.risePaySuccess(orderId);
        } else {
            // 非免费，查询是否报名成功
            if (!riseOrder.getEntry()) {
                LOGGER.error("订单:{},未支付", riseOrder.getOrderId());
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.nopaid"));
            }
        }

//        signupService.entry(courseOrder.getCourseId(), courseOrder.getClassId(), courseOrder.getOpenid());
        return WebUtils.success();
    }

    @RequestMapping(value = "/info/load", method = RequestMethod.GET)
    @Deprecated
    public ResponseEntity<Map<String, Object>> loadInfo(LoginUser loginUser) {
        InfoSubmitDto infoSubmitDto = new InfoSubmitDto();
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("提交个人信息")
                .action("加载个人信息");
        operationLogService.log(operationLog);
        Profile account = accountService.getProfile(loginUser.getId());
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
                                                          LoginUser loginUser) {
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
        profileService.submitPersonalInfo(account, false);

        Chapter chapter = courseStudyService.loadFirstChapter(infoSubmitDto.getCourseId());
        if (chapter != null) {
            chapterId = chapter.getId();
        }
        return WebUtils.result(chapterId);
    }

    @RequestMapping("/welcome/{orderId}")
    public ResponseEntity<Map<String, Object>> welcome(LoginUser loginUser, @PathVariable String orderId) {
        EntryDto entryDto = new EntryDto();
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("报名成功页面")
                .action("打开报名成功页面")
                .memo(orderId);
        operationLogService.log(operationLog);
        CourseOrder courseOrder = signupService.getOrder(orderId);
        if (courseOrder == null) {
            LOGGER.error("{} 订单不存在", orderId);
            return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.fail"));
        }
        if (courseOrder.getEntry()) {
            LOGGER.error("订单{}未支付", courseOrder.getOrderId());
            return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.nopaid"));
        }
        ClassMember classMember = signupService.classMember(orderId);
        if (classMember == null || classMember.getMemberId() == null) {
            LOGGER.error("{} 尚未报班", loginUser.getOpenId());
            return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.fail"));
        }
        entryDto.setMemberId(classMember.getMemberId());
        entryDto.setQuanwaiClass(signupService.getCachedClass(classMember.getClassId()));
        entryDto.setCourse(signupService.getCachedCourse(classMember.getCourseId()));
        Profile account = accountService.getProfile(loginUser.getOpenId(), true);
        if (account != null) {
            entryDto.setUsername(account.getNickname());
            entryDto.setHeadUrl(account.getHeadimgurl());
        } else {
            entryDto.setUsername(loginUser.getWeixinName());
            entryDto.setHeadUrl(loginUser.getHeadimgUrl());
        }
        return WebUtils.result(entryDto);
    }

    @RequestMapping(value = "/coupon/list", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> listCoupon(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        List<Coupon> coupons = signupService.getCoupons(loginUser.getId());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("报名页面")
                .action("加载优惠券");
        operationLogService.log(operationLog);
        return WebUtils.result(coupons);
    }

    /**
     * 计算优惠券
     * @param loginUser 用户信息
     * @param memberDto
     * @return
     */
    @RequestMapping(value = "/coupon/calculate", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> useCoupon(LoginUser loginUser, @RequestBody RiseMemberDto memberDto) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(memberDto.getCouponId(), "优惠券不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("报名页面")
                .action("计算优惠券减免")
                .memo(memberDto.getCouponId() + "");
        Pair<Integer, String> check = signupService.riseMemberSignupCheck(loginUser.getId(), memberDto.getMemberType());
        if (check.getLeft() != 1) {
            return WebUtils.error(check.getRight());
        }
        Double price = signupService.calculateCoupon(memberDto.getMemberType(), memberDto.getCouponId());
        operationLogService.log(operationLog);
        return WebUtils.result(price);
    }

    /**
     * 计算优惠券
     * @param loginUser 用户信息
     */
    @RequestMapping(value = "/coupon/course/calculate", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> useCoupon(LoginUser loginUser, @RequestBody RiseCourseDto riseCourseDto) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(riseCourseDto.getCouponId(), "优惠券不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("报名页面")
                .action("计算优惠券减免")
                .memo(riseCourseDto.getCouponId() + "");
        Double price = signupService.calculateCourseCoupon(riseCourseDto.getProblemId(), riseCourseDto.getCouponId());
        operationLogService.log(operationLog);
        return WebUtils.result(price);
    }

    /**
     * 小课单卖接口
     * @param loginUser 用户信息
     * @param request request请求
     * @param riseCourseDto 小课id，优惠券id(可选)
     * @return 调起H5接口的数据（如果不免费）
     */
    @RequestMapping(value = "/rise/course/pay")
    public ResponseEntity<Map<String, Object>> riseCoursePay(LoginUser loginUser, HttpServletRequest request, @RequestBody RiseCourseDto riseCourseDto) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("小课单卖")
                .action("点击支付")
                .memo(riseCourseDto.getProblemId() + "");

        operationLogService.log(operationLog);
        // 检查ip
        String remoteIp = request.getHeader("X-Forwarded-For");
        if (remoteIp == null) {
            LOGGER.error("获取用户:{} 获取IP失败:ProblemId:{}", loginUser.getOpenId(), riseCourseDto);
            remoteIp = ConfigUtils.getExternalIP();
        }
        // 检查是否需要支付
        Pair<Integer, String> check = signupService.riseCourseSignupCheck(loginUser.getId(), riseCourseDto.getProblemId());
        if (check.getLeft() != 1) {
            return WebUtils.error(check.getRight());
        }
        // 检查优惠券
        if (costRepo.checkDiscount(loginUser.getId(), riseCourseDto.getCouponId())) {
            return WebUtils.error("该优惠券无效");
        }

        // 创建订单
        QuanwaiOrder quanwaiOrder = signupService.signupRiseCourse(loginUser.getId(), riseCourseDto.getProblemId(), riseCourseDto.getCouponId());
        // 统一下单
        SignupDto signupDto = payParam(quanwaiOrder, remoteIp);
        return WebUtils.result(signupDto);
    }

    /**
     * 统一下单
     * @param quanwaiOrder 总订单
     * @param remoteIp ip
     */
    private SignupDto payParam(QuanwaiOrder quanwaiOrder, String remoteIp) {
        // 下单
        SignupDto signupDto = new SignupDto();
        signupDto.setFee(quanwaiOrder.getPrice());
        signupDto.setFree(Double.valueOf(0d).equals(quanwaiOrder.getPrice()));
        signupDto.setProductId(quanwaiOrder.getOrderId());
        if (!Double.valueOf(0).equals(quanwaiOrder.getPrice())) {
            Map<String, String> signParams = payService.buildH5PayParam(quanwaiOrder.getOrderId(), remoteIp, quanwaiOrder.getOpenid());
            signupDto.setSignParams(signParams);
            OperationLog payParamLog = OperationLog.create().openid(quanwaiOrder.getOpenid())
                    .module("报名")
                    .function("微信支付")
                    .action("下单")
                    .memo(signParams.toString());
            operationLogService.log(payParamLog);
        }
        return signupDto;
    }

    @RequestMapping(value = "/rise/member/pay")
    public ResponseEntity<Map<String, Object>> riseMemberPay(LoginUser loginUser, HttpServletRequest request, @RequestBody RiseMemberDto memberDto) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("报名页面")
                .action("点击支付")
                .memo(memberDto.getMemberType() + "");
        operationLogService.log(operationLog);
        // 检查ip
        String remoteIp = request.getHeader("X-Forwarded-For");
        if (remoteIp == null) {
            LOGGER.error("获取用户:{} 获取IP失败:CourseId:{}", loginUser.getOpenId(), memberDto);
            remoteIp = ConfigUtils.getExternalIP();
        }
        // 检查是否能够支付
        Pair<Integer, String> check = signupService.riseMemberSignupCheck(loginUser.getId(), memberDto.getMemberType());
        if (check.getLeft() != 1) {
            return WebUtils.error(check.getRight());
        }
        // 检查优惠券
        if (costRepo.checkDiscount(loginUser.getId(), memberDto.getCouponId())) {
            return WebUtils.error("该优惠券无效");
        }

        // 创建订单
        QuanwaiOrder quanwaiOrder = signupService.signupRiseMember(loginUser.getId(),
                memberDto.getMemberType(), memberDto.getCouponId());
        // 下单
        SignupDto signupDto = this.payParam(quanwaiOrder, remoteIp);
        return WebUtils.result(signupDto);
    }

    @RequestMapping(value = "/rise/member", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getRiseMemberPayInfo(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("报名页面")
                .action("加载Rise会员信息");
        operationLogService.log(operationLog);
        List<MemberType> memberTypesPayInfo = signupService.getMemberTypesPayInfo();
        // 查看优惠券信息
        List<Coupon> coupons = signupService.getCoupons(loginUser.getId());
        RiseMemberDto dto = new RiseMemberDto();
        dto.setMemberTypes(memberTypesPayInfo);
        dto.setCoupons(coupons);
        return WebUtils.result(dto);
    }

    @RequestMapping(value = "/rise/member/check/{memberTypeId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> checkRiseMemberDate(LoginUser loginUser, @PathVariable Integer memberTypeId) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("报名页面")
                .action("点击RISE会员选择按钮")
                .memo(memberTypeId + "");
        operationLogService.log(operationLog);
        Pair<Integer, String> result = signupService.riseMemberSignupCheckNoHold(loginUser.getId(), memberTypeId);
        if (result.getLeft() != 1) {
            if (result.getLeft() == -4) {
                return WebUtils.error(214, result.getRight());
            } else {
                return WebUtils.error(result.getRight());
            }
        } else {
            return WebUtils.success();
        }
    }

    @RequestMapping(value = "/mark/normal/question")
    public ResponseEntity<Map<String, Object>> markNormalQuestion(LoginUser loginUser) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("打点")
                .action("打开常见问题");
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/mark/pay/{type}")
    public ResponseEntity<Map<String, Object>> markPayErr(LoginUser loginUser, @PathVariable String type) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("打点")
                .action("支付")
                .memo(type);
        operationLogService.log(operationLog);
        return WebUtils.success();
    }
}
