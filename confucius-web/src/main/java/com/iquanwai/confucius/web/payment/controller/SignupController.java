package com.iquanwai.confucius.web.payment.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.backend.BusinessSchoolService;
import com.iquanwai.confucius.biz.domain.course.signup.CostRepo;
import com.iquanwai.confucius.biz.domain.course.signup.RiseMemberManager;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.fragmentation.CacheService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayService;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.apply.BusinessApplySubmit;
import com.iquanwai.confucius.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.BusinessSchoolApplicationOrder;
import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampOrder;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.po.fragmentation.RiseOrder;
import com.iquanwai.confucius.biz.po.fragmentation.course.CourseConfig;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.ErrorMessageUtils;
import com.iquanwai.confucius.web.payment.dto.ApplySubmitDto;
import com.iquanwai.confucius.web.payment.dto.CampInfoDto;
import com.iquanwai.confucius.web.payment.dto.GoodsInfoDto;
import com.iquanwai.confucius.web.payment.dto.MonthlyCampProcessDto;
import com.iquanwai.confucius.web.payment.dto.PaymentDto;
import com.iquanwai.confucius.web.payment.dto.RiseMemberDto;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.resolver.UnionUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/9/10.
 */
@RestController
@RequestMapping("/signup")
public class SignupController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private SignupService signupService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private PayService payService;
    @Autowired
    private CostRepo costRepo;
    @Autowired
    private MessageService messageService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private BusinessSchoolService businessSchoolService;
    @Autowired
    private RiseMemberManager riseMemberManager;


    /**
     * rise产品支付成功的回调
     *
     * @param loginUser 用户信息
     * @param orderId   订单id
     * @return 处理结果
     */
    @RequestMapping(value = "/paid/rise/{orderId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> riseMemberPaid(LoginUser loginUser, @PathVariable String orderId) {
        Assert.notNull(loginUser, "用户不能为空");
        QuanwaiOrder quanwaiOrder = signupService.getQuanwaiOrder(orderId);

        Boolean entry;
        switch (quanwaiOrder.getGoodsType()) {
            case QuanwaiOrder.FRAG_MEMBER:
                // 会员购买
                RiseOrder riseOrder = signupService.getRiseOrder(orderId);
                if (riseOrder == null) {
                    logger.error("{} 订单不存在", orderId);
                    return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.fail"));
                } else {
                    entry = riseOrder.getEntry();
                }
                break;
            case QuanwaiOrder.FRAG_CAMP:
                // 专项课购买
                MonthlyCampOrder campOrder = signupService.getMonthlyCampOrder(orderId);
                if (campOrder == null) {
                    logger.error("{} 订单不存在", orderId);
                    return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.fail"));
                } else {
                    entry = campOrder.getEntry();
                }
                break;
            case QuanwaiOrder.BS_APPLICATION:
                // 商学院申请购买
                BusinessSchoolApplicationOrder bsOrder = signupService.getBusinessSchoolOrder(orderId);
                if (bsOrder == null) {
                    logger.error("{} 订单不存在", orderId);
                    return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.fail"));
                } else {
                    entry = bsOrder.getPaid();
                }
                break;
            default:
                logger.error("{} 订单类型异常", orderId);
                return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.fail"));
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
                payService.paySuccess(orderId);
            } else {
                // 非免费，查询是否报名成功
                if (!entry) {
                    logger.error("订单:{},未支付", orderId);
                    messageService.sendAlarm("报名模块出错", "订单未支付",
                            "高", "订单id:" + orderId, "订单未支付，却进行了支付完成操作");
                    return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.nopaid"));
                }
            }

        } catch (Exception e) {
            logger.error("报名出错", e);
            messageService.sendAlarm("报名模块出错", "运行时异常",
                    "高", "订单id:" + orderId, e.getLocalizedMessage());
        }

        return WebUtils.success();
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

    private Pair<Integer, Integer> calcDealTime(Integer memberTypeId, Integer profileId) {

        BusinessSchoolApplication businessSchoolApplication = accountService.loadLastApply(profileId, memberTypeId);
        if (businessSchoolApplication == null) {
            return Pair.of(24, 0);
        } else {
            int time = DateUtils.intervalMinute(DateUtils.afterHours(businessSchoolApplication.getDealTime(), 24));
            if (time <= 0) {
                businessSchoolService.expiredApply(businessSchoolApplication.getId());
                return Pair.of(0, 0);
            } else {
                return Pair.of(time / 60, time % 60);
            }
        }
    }

    @RequestMapping(value = "/rise/member/check/{memberTypeId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> checkRiseMemberDate(UnionUser loginUser, @PathVariable Integer memberTypeId) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("报名页面")
                .action("点击RISE会员选择按钮")
                .memo(memberTypeId + "");
        operationLogService.log(operationLog);
        RiseMemberDto dto = new RiseMemberDto();
        // 检查是否有报名权限
        Pair<Boolean, String> result = signupService.risePurchaseCheck(loginUser.getId(), memberTypeId);
        dto.setPrivilege(result.getLeft());
        dto.setErrorMsg(result.getRight());

        // 检查是否关注
        Account account = accountService.getAccountByUnionId(loginUser.getUnionId());
        Boolean subscribe = account != null && account.getSubscribe() == 1;
        if (!subscribe) {
            String qrCodeUrl = signupService.getSubscribeQrCodeForPay(memberTypeId);
            dto.setQrCode(qrCodeUrl);
        }
        dto.setSubscribe(subscribe);
        return WebUtils.result(dto);
    }

    @RequestMapping(value = "/mark/pay/{function}/{action}")
    public ResponseEntity<Map<String, Object>> markPayErr(LoginUser loginUser, @PathVariable(value = "function") String function,
                                                          @PathVariable(value = "action") String action, @RequestParam(required = false) String param) {
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
     *
     * @param loginUser    用户
     * @param goodsInfoDto 商品信息
     * @return 详细的商品信息
     */
    @RequestMapping(value = "/load/goods", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> loadGoodsInfo(LoginUser loginUser, @RequestBody GoodsInfoDto goodsInfoDto) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(goodsInfoDto, "商品信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("报名页面")
                .action("获取商品信息")
                .memo(goodsInfoDto.getGoodsType());
        operationLogService.log(operationLog);

        // 是否能使用多个优惠券
        goodsInfoDto.setMultiCoupons(this.checkMultiCoupons(goodsInfoDto.getGoodsType()));
        // 计算价格/等特殊
        MemberType memberType = signupService.getMemberTypePayInfo(loginUser.getId(), goodsInfoDto.getGoodsId());
        if (memberType != null) {
            goodsInfoDto.setInitPrice(memberType.getInitPrice());
            goodsInfoDto.setFee(memberType.getFee());
            // TODO 交换description和name
            goodsInfoDto.setName(memberType.getName());
        }

        // 获取优惠券
        if (canUseCoupon(goodsInfoDto)) {
            List<Coupon> coupons = signupService.getCoupons(loginUser.getId());
            goodsInfoDto.setCoupons(coupons);
            // 自动选择优惠券
            List<Coupon> autoCoupons = signupService.autoChooseCoupon(
                    goodsInfoDto.getGoodsType(), goodsInfoDto.getFee(), coupons);
            goodsInfoDto.setAutoCoupons(autoCoupons);
        } else {
            goodsInfoDto.setCoupons(Lists.newArrayList());
            goodsInfoDto.setAutoCoupons(Lists.newArrayList());
        }


        return WebUtils.result(goodsInfoDto);
    }

    private Boolean checkMultiCoupons(String goodsType) {
        switch (goodsType) {
            case QuanwaiOrder.FRAG_MEMBER:
                return true;
            default:
                return false;
        }
    }

    /**
     * 获取H5支付参数的接口
     *
     * @param loginUser  用户
     * @param request    request对象
     * @param paymentDto 商品类型以及商品id
     * @return 支付参数
     */
    @RequestMapping(value = "/load/pay/param")
    public ResponseEntity<Map<String, Object>> loadPayParam(LoginUser loginUser, HttpServletRequest request, @RequestBody PaymentDto paymentDto) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(paymentDto, "支付信息不能为空");

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("报名页面")
                .action("点击支付")
                .memo(paymentDto.getGoodsType());
        operationLogService.log(operationLog);

        // 检查ip
        String remoteIp = request.getHeader("X-Forwarded-For");
        if (remoteIp == null) {
            remoteIp = ConfigUtils.getExternalIP();
        }

        Pair<Boolean, String> check = signupService.risePurchaseCheck(loginUser.getId(), paymentDto.getGoodsId());

        if (!check.getLeft()) {
            return WebUtils.error(check.getRight());
        }

        if (CollectionUtils.isNotEmpty(paymentDto.getCouponsIdGroup())) {
            for (Integer coupon : paymentDto.getCouponsIdGroup()) {
                if (!costRepo.checkCouponValidation(loginUser.getId(), coupon)) {
                    return WebUtils.error("该优惠券无效");
                }
            }
        }

        // 根据前端传进来的 param 创建订单信息
        QuanwaiOrder quanwaiOrder = this.createQuanwaiOrder(paymentDto, loginUser.getId());
        // 下单
        PaymentDto paymentParam;
        if (paymentDto.getPayType() == QuanwaiOrder.PAY_WECHAT) {
            paymentParam = this.createPayParam(quanwaiOrder, remoteIp, loginUser.getOpenId());
        } else if (paymentDto.getPayType() == QuanwaiOrder.PAY_ALI) {
            paymentParam = this.createAlipay(quanwaiOrder, loginUser.getOpenId());
        } else {
            return WebUtils.error("支付方式异常");
        }
        return WebUtils.result(paymentParam);
    }

    /**
     * 计算优惠券，返回优惠价格
     *
     * @param loginUser 用户信息
     */
    @RequestMapping(value = "/payment/coupon/calculate", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> calculateCoupons(LoginUser loginUser, @RequestBody PaymentDto paymentDto) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(paymentDto, "支付信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("报名页面")
                .action("计算优惠券减免");
        operationLogService.log(operationLog);
        Double price = signupService.calculateMemberCoupon(loginUser.getId(), paymentDto.getGoodsId(), paymentDto.getCouponsIdGroup());
        return WebUtils.result(price);
    }

    @RequestMapping(value = "/current/camp/month", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> validateCampUrl(LoginUser loginUser) {
        Assert.notNull(loginUser, "登录用户不能为空");
        MonthlyCampProcessDto dto = new MonthlyCampProcessDto();
        CourseConfig monthlyCampConfig = cacheService.loadCourseConfig(RiseMember.CAMP);
        Integer currentSellingMonth = monthlyCampConfig.getSellingMonth();
        dto.setMarkSellingMemo(monthlyCampConfig.getSellingYear() + "-" + monthlyCampConfig.getSellingMonth());
        dto.setCurrentCampMonth(currentSellingMonth);
        dto.setCampMonthProblemId(signupService.loadHrefProblemId(loginUser.getId(), currentSellingMonth));
        return WebUtils.result(dto);
    }

    /**
     * 创建订单
     *
     * @param paymentDto 支付信息
     * @param profileId  用户id
     * @return 订单对象
     */
    private QuanwaiOrder createQuanwaiOrder(PaymentDto paymentDto, Integer profileId) {
        // TODO 将来删除
        switch (paymentDto.getGoodsType()) {
            case QuanwaiOrder.FRAG_MEMBER: {
                return signupService.signUpRiseMember(profileId, paymentDto.getGoodsId(), paymentDto.getCouponsIdGroup(), paymentDto.getPayType());
            }
            case QuanwaiOrder.FRAG_CAMP: {
                Integer couponId = null;
                if (CollectionUtils.isNotEmpty(paymentDto.getCouponsIdGroup())) {
                    couponId = paymentDto.getCouponsIdGroup().get(0);
                }
                return signupService.signUpMonthlyCamp(profileId, paymentDto.getGoodsId(), couponId, paymentDto.getPayType());
            }
            case QuanwaiOrder.BS_APPLICATION: {
                Integer couponId = null;
                if (CollectionUtils.isNotEmpty(paymentDto.getCouponsIdGroup())) {
                    couponId = paymentDto.getCouponsIdGroup().get(0);
                }
                return signupService.signupBusinessSchoolApplication(profileId, paymentDto.getGoodsId(), couponId, paymentDto.getPayType());
            }
            default:
                logger.error("异常，用户:{} 的商品类型未知:{}", profileId, paymentDto);
                return null;
        }
    }

    /**
     * 1. 预先在 QuanwaiOrder 表中生成了订单记录，但不是真正用来发送到微信的订单，现在要创建即将往微信发送的订单参数
     * 2. 这个参数只能在后端生成，然后作为参数返回给前端，前端拿到了参数直接调用。
     * 3. 备注：调用微信支付之后，微信的回调 URI 也是在这边配置
     */
    private PaymentDto createPayParam(QuanwaiOrder quanwaiOrder, String remoteIp, String openid) {
        // 下单
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setFee(quanwaiOrder.getPrice());
        paymentDto.setFree(Double.valueOf(0d).equals(quanwaiOrder.getPrice()));
        paymentDto.setProductId(quanwaiOrder.getOrderId());
        if (!Double.valueOf(0).equals(quanwaiOrder.getPrice())) {
            Map<String, String> signParams = payService.buildH5PayParam(quanwaiOrder.getOrderId(), remoteIp, openid);
            paymentDto.setSignParams(signParams);
            OperationLog payParamLog = OperationLog.create().openid(openid)
                    .module("报名")
                    .function("微信支付")
                    .action("下单")
                    .memo(signParams.toString());
            operationLogService.log(payParamLog);
        }
        return paymentDto;
    }

    /**
     * 阿里支付
     *
     * @param quanwaiOrder 订单对象
     * @return 支付参数
     */
    private PaymentDto createAlipay(QuanwaiOrder quanwaiOrder, String openid) {
        // 下单
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setFee(quanwaiOrder.getPrice());
        paymentDto.setFree(Double.valueOf(0d).equals(quanwaiOrder.getPrice()));
        paymentDto.setProductId(quanwaiOrder.getOrderId());
        if (!Double.valueOf(0).equals(quanwaiOrder.getPrice())) {
            String postPayString = payService.buildAlipayParam(quanwaiOrder);
            Map<String, String> signParams = Maps.newHashMap();
            signParams.put("alipayUrl", postPayString);
            paymentDto.setSignParams(signParams);
            OperationLog payParamLog = OperationLog.create().openid(openid)
                    .module("报名")
                    .function("支付宝支付")
                    .action("下单")
                    .memo(signParams.toString());
            operationLogService.log(payParamLog);
        }
        return paymentDto;
    }


    @RequestMapping("/rise/member/{memberTypeId}")
    public ResponseEntity<Map<String, Object>> riseMember(@PathVariable Integer memberTypeId, LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("报名页面")
                .action("加载Rise会员信息")
                .memo(memberTypeId.toString());
        operationLogService.log(operationLog);

        // pre1.获取基本信息（所有会员数据、要购买商品类型）
        List<RiseMember> allUserMembers = riseMemberManager.loadPersonalAllRiseMembers(loginUser.getId());
        MemberType memberType = signupService.getMemberTypePayInfo(loginUser.getId(), memberTypeId);
        if (memberType == null) {
            return WebUtils.error("商品类型错误");
        }
        // 1.检查是否有权限
        Pair<Boolean, String> pass = signupService.risePurchaseCheck(loginUser.getId(), memberTypeId);
        // 2.获取相关数据
        List<RiseMember> riseMembers = allUserMembers.stream().filter(item -> !item.getExpired()).collect(Collectors.toList());
        // 3.拼装dto
        RiseMemberDto dto = new RiseMemberDto();
        dto.setPrivilege(pass.getLeft());
        dto.setErrorMsg(pass.getRight());
        dto.setMemberType(memberType);

        // 不同商品的特殊逻辑
        if (memberType.getId() == RiseMember.ELITE) {
            // TODO 页面按钮文字写死,tips 也删掉
            dto.setTip("开学后7天内可全额退款");
            RiseMember noMbaRiseMember = riseMembers.stream().filter(item -> !item.getMemberTypeId().equals(RiseMember.BUSINESS_THOUGHT)).findFirst().orElse(null);
            if (noMbaRiseMember != null && noMbaRiseMember.getMemberTypeId() != null) {
                if (noMbaRiseMember.getMemberTypeId().equals(RiseMember.HALF) || noMbaRiseMember.getMemberTypeId().equals(RiseMember.ANNUAL)) {
                    dto.setButtonStr("升级商学院");
                    dto.setTip("优秀学员学费已减免，一键升级商学院");
                } else if (noMbaRiseMember.getMemberTypeId().equals(RiseMember.HALF_ELITE)) {
                    // 如果是精英版半年用户，提供续费通道，转成商学院 1 年
                    dto.setTip(null);
                    dto.setButtonStr("续费商学院");
                } else if (noMbaRiseMember.getMemberTypeId() == RiseMember.ELITE) {
                    //商学院用户不显示按钮
                    dto.setButtonStr("立即入学");
//                    return WebUtils.result(dto);
                } else {
                    dto.setButtonStr("立即入学");
                    dto.setAuditionStr("预约体验");
                }
            } else {
                dto.setButtonStr("立即入学");
                dto.setAuditionStr("预约体验");
            }

            // 用户层级是商学院用户或者曾经是商学院用户，则不显示试听课入口
            Long count = allUserMembers.stream().filter(member -> member.getMemberTypeId() == RiseMember.ELITE).count();
            if (count > 0) {
                // 不显示宣讲会按钮
                dto.setAuditionStr(null);
            }
            if (pass.getLeft()) {
                // 有付费权限不显示宣讲会按钮
                dto.setAuditionStr(null);
            }
        } else if (memberType.getId() == RiseMember.BUSINESS_THOUGHT) {
            // ignore
        } else if (memberType.getId() == RiseMember.BS_APPLICATION) {
            // ignore
        }
        // 计算过期时间
        Pair<Integer, Integer> hourMinutes = calcDealTime(memberTypeId, loginUser.getId());
        dto.setRemainHour(hourMinutes.getLeft());
        dto.setRemainMinute(hourMinutes.getRight());
        return WebUtils.result(dto);
    }

    @RequestMapping("/rise/member/entry/{memberTypeId}")
    public ResponseEntity<Map<String, Object>> entryRiseMember(@PathVariable Integer memberTypeId, LoginUser loginUser) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("用户信息")
                .function("报名页面")
                .action("查询用户报名信息")
                .memo(String.valueOf(memberTypeId));
        operationLogService.log(operationLog);

        RiseMember riseMember = riseMemberManager.member(loginUser.getId()).stream().filter(item -> item.getMemberTypeId().equals(memberTypeId)).findFirst().orElse(null);
        Profile profile = accountService.getProfile(loginUser.getId());
        if (riseMember != null) {
            riseMember.setEntryCode(profile.getMemberId());
            return WebUtils.result(riseMember.simple());
        } else {
            return WebUtils.error("会员类型校验出错");
        }
    }

    @RequestMapping("/rise/preacher/number")
    public ResponseEntity<Map<String, Object>> getRisePreacherNumber(LoginUser loginUser) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("课程信息")
                .function("RISE")
                .action("宣讲课数字");
        operationLogService.log(operationLog);
        Date date = new DateTime().withDayOfMonth(1).toDate();
        return WebUtils.result(DateUtils.parseDateToFormat7(date));
    }

    @RequestMapping(value = "/guest/camp/sell/info", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getCampSellInfo() {
        logger.info("查询专项课售卖页信息");
        String json = ConfigUtils.getCampPayInfo();
        if (json == null) {
            return WebUtils.error("配置异常");
        } else {
            CourseConfig monthlyCampConfig = cacheService.loadCourseConfig(RiseMember.CAMP);
            CampInfoDto dto = JSONObject.parseObject(json, CampInfoDto.class);
            dto.setMarkSellingMemo(monthlyCampConfig.getSellingYear() + "-" + monthlyCampConfig.getSellingMonth());
            dto.setCurrentCampMonth(monthlyCampConfig.getSellingMonth());
            return WebUtils.result(dto);
        }
    }

    @RequestMapping(value = "/order/success", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> orderQuerySuccess(LoginUser loginUser, @RequestParam String orderId) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("订单")
                .function("支付状态")
                .action("查询是否支付成功")
                .memo(orderId);
        operationLogService.log(operationLog);
        QuanwaiOrder quanwaiOrder = signupService.getQuanwaiOrder(orderId);
        if (quanwaiOrder != null && quanwaiOrder.getStatus() == QuanwaiOrder.PAID) {
            PaymentDto dto = new PaymentDto();
            dto.setGoodsId(Integer.parseInt(quanwaiOrder.getGoodsId()));
            dto.setGoodsType(quanwaiOrder.getGoodsType());
            return WebUtils.result(dto);
        } else {
            return WebUtils.error("还没有支付");
        }
    }


    /**
     * 商学院申请信息提交
     *
     * @param loginUser      用户
     * @param applySubmitDto 申请信息
     */
    @RequestMapping(value = "/submit/apply", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitApply(LoginUser loginUser, @RequestBody ApplySubmitDto applySubmitDto) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("商学院")
                .function("申请")
                .action("提交申请");
        operationLogService.log(operationLog);

        Pair<Boolean, String> pass = signupService.risePurchaseCheck(loginUser.getId(), applySubmitDto.getGoodsId());
        if (!pass.getLeft()) {
            return WebUtils.error(pass.getRight());
        }

        // 提交申请信息
        List<BusinessApplySubmit> userApplySubmits = applySubmitDto.getUserSubmits().stream().map(applySubmitVO -> {
            BusinessApplySubmit submit = new BusinessApplySubmit();
            submit.setQuestionId(applySubmitVO.getQuestionId());
            submit.setChoiceId(applySubmitVO.getChoiceId());
            submit.setUserValue(applySubmitVO.getUserValue());
            return submit;
        }).collect(Collectors.toList());
        // 如果不需要支付，则直接有效，否则先设置为无效
        businessSchoolService.submitBusinessApply(loginUser.getId(), userApplySubmits, !ConfigUtils.getPayApplyFlag(), applySubmitDto.getGoodsId());
        return WebUtils.success();
    }


    private boolean canUseCoupon(GoodsInfoDto goodsInfoDto) {
        //申请商学院不能用优惠券
        if (goodsInfoDto.getGoodsType().equals(QuanwaiOrder.BS_APPLICATION)) {
            return false;
        }

        return true;
    }


}
