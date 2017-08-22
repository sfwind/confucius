package com.iquanwai.confucius.web.course.controller;

import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.domain.course.progress.CourseStudyService;
import com.iquanwai.confucius.biz.domain.course.signup.CostRepo;
import com.iquanwai.confucius.biz.domain.course.signup.CourseReductionService;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.customer.ProfileService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayService;
import com.iquanwai.confucius.biz.exception.ErrorConstants;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.common.customer.CourseReductionActivity;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
import com.iquanwai.confucius.biz.po.fragmentation.RiseCourseOrder;
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
import com.iquanwai.confucius.web.course.dto.*;
import com.iquanwai.confucius.web.course.dto.payment.GoodsInfoDto;
import com.iquanwai.confucius.web.course.dto.payment.PaymentDto;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
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
import org.springframework.web.bind.annotation.RequestParam;
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
    @Autowired
    private MessageService messageService;
    @Autowired
    private PlanService planService;
    @Autowired
    private CourseReductionService courseReductionService;

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
     * 训练营支付成功后调用，用于处理后续操作
     * @param loginUser 用户
     * @param orderId 订单id
     * @return 执行结果
     */
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
        return WebUtils.success();
    }

    /**
     * rise产品支付成功的回调
     * @param loginUser 用户信息
     * @param orderId 订单id
     * @return 处理结果
     */
    @RequestMapping(value = "/paid/rise/{orderId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> riseMemberPaid(LoginUser loginUser, @PathVariable String orderId) {
        Assert.notNull(loginUser, "用户不能为空");
        QuanwaiOrder quanwaiOrder = signupService.getQuanwaiOrder(orderId);
        Boolean entry;
        Integer problemId = null;
        Integer planId = null;
        if (quanwaiOrder.getGoodsType().equals(QuanwaiOrder.FRAG_MEMBER)) {
            // 会员购买
            RiseOrder riseOrder = signupService.getRiseOrder(orderId);
            if (riseOrder == null) {
                LOGGER.error("{} 订单不存在", orderId);
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.fail"));
            } else {
                entry = riseOrder.getEntry();
            }
        } else {
            // 其他为小课购买
            RiseCourseOrder riseCourseOrder = signupService.getRiseCourse(orderId);
            if (riseCourseOrder == null) {
                LOGGER.error("{} 订单不存在", orderId);
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.fail"));
            } else {
                entry = riseCourseOrder.getEntry();
            }
            problemId = riseCourseOrder.getProblemId();
        }
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("付费完成")
                .action("点击付费完成")
                .memo(orderId);
        operationLogService.log(operationLog);
        Double zero = 0d;
        try {
            if (zero.equals(quanwaiOrder.getPrice())) {
                // 免费，自动报名
                payService.handlePayResult(orderId, true);
                payService.risePaySuccess(orderId);
            } else {
                // 非免费，查询是否报名成功
                if (!entry) {
                    LOGGER.error("订单:{},未支付", orderId);
                    messageService.sendAlarm("报名模块出错", "订单未支付",
                            "高", "订单id:" + orderId, "订单未支付，却进行了支付完成操作");
                    return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.nopaid"));
                }
            }
            if (problemId != null) {
                ImprovementPlan plan = planService.loadPlanByProblemId(loginUser.getId(), problemId);
                if (plan != null) {
                    planId = plan.getId();
                }
            }
        } catch (Exception e) {
            LOGGER.error("报名出错", e);
            messageService.sendAlarm("报名模块出错", "运行时异常",
                    "高", "订单id:" + orderId, e.getLocalizedMessage());
        }

        return WebUtils.result(planId);
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
        Double price = signupService.calculateCourseCoupon(riseCourseDto.getProblemId(), loginUser.getId(), riseCourseDto.getCouponId());
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
        if (!costRepo.checkDiscount(loginUser.getId(), riseCourseDto.getCouponId())) {
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
            messageService.sendAlarm("报名模块出错", "获取用户:" + loginUser.getId() + " IP失败",
                    "高", "会员类型:" + memberDto.getMemberType(), "IP获取失败");
            remoteIp = ConfigUtils.getExternalIP();
        }
        // 检查是否能够支付
        Pair<Integer, String> check = signupService.riseMemberSignupCheck(loginUser.getId(), memberDto.getMemberType());
        if (check.getLeft() != 1) {
            return WebUtils.error(check.getRight());
        }
        // 检查优惠券
        if (!costRepo.checkDiscount(loginUser.getId(), memberDto.getCouponId())) {
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

    @RequestMapping(value = "/mark/pay/{function}/{action}")
    public ResponseEntity<Map<String, Object>> markPayErr(LoginUser loginUser, @PathVariable(value = "function") String function, @PathVariable(value = "action") String action, @RequestParam(required = false) String param) {
        String memo = "";
        if (param != null) {
            if (param.length() > 1024) {
                memo = param.substring(0, 1024);
            } else {
                memo = param;
            }
        }
        messageService.sendAlarm("报名模块出错", "订单支付失败",
                "高", "profileId:" + loginUser.getId(), memo);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("支付")
                .function(function)
                .action(action)
                .memo(memo);
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    /**
     * 获取商品信息
     * @param loginUser 用户
     * @param goodsInfoDto 商品信息
     * @return 详细的商品信息
     */
    @RequestMapping(value = "/load/goods", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> loadGoodsInfo(LoginUser loginUser, @RequestBody GoodsInfoDto goodsInfoDto) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(goodsInfoDto, "商品信息不能为空");
        if (!GoodsInfoDto.GOODS_TYPES.contains(goodsInfoDto.getGoodsType())) {
            LOGGER.error("获取商品信息的商品类型异常,{}", goodsInfoDto);
            return WebUtils.error("商品类型异常");
        }
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后端")
                .function("报名页面")
                .action("获取商品信息")
                .memo(goodsInfoDto.getGoodsType());
        operationLogService.log(operationLog);
        if (GoodsInfoDto.FRAG_COURSE.equals(goodsInfoDto.getGoodsType())) {
            // 小课购买
            goodsInfoDto.setName("小课购买");
            // 查看该用户是否参加了减免优惠活动
            CourseReductionActivity activity = courseReductionService.loadRecentCourseReduction(loginUser.getId(), goodsInfoDto.getGoodsId());
            goodsInfoDto.setFee(ConfigUtils.getRiseCourseFee());
            if (activity != null) {
                goodsInfoDto.setActivity(activity);
            }
        } else if(GoodsInfoDto.FRAG_MEMBER.equals(goodsInfoDto.getGoodsType())) {
            if(GoodsInfoDto.FRAG_COURSE.equals(goodsInfoDto.getGoodsType())) {
                goodsInfoDto.setName("会员购买");
            } else if(GoodsInfoDto.FRAG_CAMP.equals(goodsInfoDto.getGoodsType())) {
                goodsInfoDto.setName("训练营小课购买");
            }
            // 会员购买
            MemberType memberType = signupService
                    .getMemberTypesPayInfo()
                    .stream()
                    .filter(item -> item.getId().equals(goodsInfoDto.getGoodsId()))
                    .findFirst()
                    .orElse(null);
            if (memberType == null) {
                LOGGER.error("会员类型异常{}", goodsInfoDto);
                return WebUtils.error("会员类型异常");
            } else {
                goodsInfoDto.setFee(memberType.getFee());
                goodsInfoDto.setStartTime(memberType.getStartTime());
                goodsInfoDto.setEndTime(memberType.getEndTime());
            }
        }

        // 获取优惠券
        List<Coupon> coupons = signupService.getCoupons(loginUser.getId());
        goodsInfoDto.setCoupons(coupons);
        return WebUtils.result(goodsInfoDto);
    }

    /**
     * 获取H5支付参数的接口
     * @param loginUser 用户
     * @param request request对象
     * @param paymentDto 商品类型以及商品id
     * @return 支付参数
     */
    @RequestMapping(value = "/load/pay/param")
    public ResponseEntity<Map<String, Object>> loadPayParam(LoginUser loginUser, HttpServletRequest request, @RequestBody PaymentDto paymentDto) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(paymentDto, "支付信息不能为空");
        if (!GoodsInfoDto.GOODS_TYPES.contains(paymentDto.getGoodsType())) {
            LOGGER.error("获取商品信息的商品类型异常,{}", paymentDto);
            return WebUtils.error("商品类型异常");
        }

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后端")
                .function("报名页面")
                .action("点击支付")
                .memo(paymentDto.getGoodsType());
        operationLogService.log(operationLog);

        // 检查ip
        String remoteIp = request.getHeader("X-Forwarded-For");
        if (remoteIp == null) {
            remoteIp = ConfigUtils.getExternalIP();
        }

        // 检查是否能够支付
        Pair<Integer, String> check = this.signupCheck(paymentDto, loginUser.getId());
        if (check.getLeft() != 1) {
            return WebUtils.error(check.getRight());
        }

        // 检查优惠券
        if (!costRepo.checkDiscount(loginUser.getId(), paymentDto.getCouponId())) {
            return WebUtils.error("该优惠券无效");
        }

        // 根据前端传进来的 param 创建订单信息
        QuanwaiOrder quanwaiOrder = this.createQuanwaiOrder(paymentDto, loginUser.getId());
        // 下单
        PaymentDto paymentParam = this.createPayParam(quanwaiOrder, remoteIp);
        return WebUtils.result(paymentParam);
    }

    /**
     * 计算优惠券
     * @param loginUser 用户信息
     */
    @RequestMapping(value = "/payment/coupon/calculate", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> calculateCoupons(LoginUser loginUser, @RequestBody PaymentDto paymentDto) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(paymentDto.getCouponId(), "优惠券不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("报名页面")
                .action("计算优惠券减免")
                .memo(paymentDto.getCouponId() + "");
        operationLogService.log(operationLog);

        Double price;
        switch (paymentDto.getGoodsType()) {
            case GoodsInfoDto.FRAG_COURSE:
                price = signupService.calculateCourseCoupon(paymentDto.getGoodsId(), loginUser.getId(), paymentDto.getCouponId());
                return WebUtils.result(price);
            case GoodsInfoDto.FRAG_MEMBER:
                Pair<Integer, String> check = signupService.riseMemberSignupCheck(loginUser.getId(), paymentDto.getGoodsId());
                if (check.getLeft() != 1) {
                    return WebUtils.error(check.getRight());
                }
                price = signupService.calculateCoupon(paymentDto.getGoodsId(), paymentDto.getCouponId());
                return WebUtils.result(price);

            default:
                LOGGER.error("异常，用户:{}商品类型有问题:{}", loginUser.getId(), paymentDto);
                return WebUtils.error("商品类型异常");
        }
    }

    /**
     * 创建订单
     * @param paymentDto 支付信息
     * @param profileId 用户id
     * @return 订单对象
     */
    private QuanwaiOrder createQuanwaiOrder(PaymentDto paymentDto, Integer profileId) {
        switch (paymentDto.getGoodsType()) {
            case GoodsInfoDto.FRAG_COURSE: {
                return signupService.signupRiseCourse(profileId, paymentDto.getGoodsId(), paymentDto.getCouponId());
            }
            case GoodsInfoDto.FRAG_MEMBER: {
                return signupService.signupRiseMember(profileId, paymentDto.getGoodsId(), paymentDto.getCouponId());
            }
            case GoodsInfoDto.FRAG_CAMP: {
                return signupService.signupMonthlyCamp(profileId, paymentDto.getCouponId());
            }
            default:
                LOGGER.error("异常，用户:{} 的商品类型未知:{}", profileId, paymentDto);
                return null;
        }
    }

    /**
     * 支付检查
     * @param paymentDto 支付信息
     * @param profileId 用户id
     * @return 检查结果
     */
    private Pair<Integer, String> signupCheck(PaymentDto paymentDto, Integer profileId) {
        switch (paymentDto.getGoodsType()) {
            case GoodsInfoDto.FRAG_COURSE: {
                // 购买小课
                return signupService.riseMemberSignupCheck(profileId, paymentDto.getGoodsId());
            }
            case GoodsInfoDto.FRAG_MEMBER: {
                // 购买会员
                return signupService.riseCourseSignupCheck(profileId, paymentDto.getGoodsId());
            }
            case GoodsInfoDto.FRAG_CAMP: {
                // 购买训练营小课
                return signupService.risePurchaseCheck(profileId, paymentDto.getGoodsId());
            }
            default:
                LOGGER.error("异常，用户:{} 的商品类型未知:{}", profileId, paymentDto);
                return new MutablePair<>(-1, "会员类型异常");
        }
    }

    /**
     * 1. 预先在 QuanwaiOrder 表中生成了订单记录，但不是真正用来发送到微信的订单，现在要创建即将往微信发送的订单参数
     * 2. 这个参数只能在后端生成，然后作为参数返回给前端，前端拿到了参数直接调用。
     * 3. 备注：调用微信支付之后，微信的回调 URI 也是在这边配置
     */
    private PaymentDto createPayParam(QuanwaiOrder quanwaiOrder, String remoteIp) {
        // 下单
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setFee(quanwaiOrder.getPrice());
        paymentDto.setFree(Double.valueOf(0d).equals(quanwaiOrder.getPrice()));
        paymentDto.setProductId(quanwaiOrder.getOrderId());
        if (!Double.valueOf(0).equals(quanwaiOrder.getPrice())) {
            Map<String, String> signParams = payService.buildH5PayParam(quanwaiOrder.getOrderId(), remoteIp, quanwaiOrder.getOpenid());
            paymentDto.setSignParams(signParams);
            OperationLog payParamLog = OperationLog.create().openid(quanwaiOrder.getOpenid())
                    .module("报名")
                    .function("微信支付")
                    .action("下单")
                    .memo(signParams.toString());
            operationLogService.log(payParamLog);
        }
        return paymentDto;
    }
}
