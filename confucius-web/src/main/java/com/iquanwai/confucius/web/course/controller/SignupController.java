package com.iquanwai.confucius.web.course.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.backend.BusinessSchoolService;
import com.iquanwai.confucius.biz.domain.course.signup.BusinessSchool;
import com.iquanwai.confucius.biz.domain.course.signup.CostRepo;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.fragmentation.CacheService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayService;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.AuditionClassMember;
import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampConfig;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampOrder;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.po.fragmentation.RiseOrder;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.ErrorMessageUtils;
import com.iquanwai.confucius.web.course.dto.InfoSubmitDto;
import com.iquanwai.confucius.web.course.dto.RiseMemberDto;
import com.iquanwai.confucius.web.course.dto.backend.MonthlyCampProcessDto;
import com.iquanwai.confucius.web.course.dto.payment.BusinessSchoolDto;
import com.iquanwai.confucius.web.course.dto.payment.GoodsInfoDto;
import com.iquanwai.confucius.web.course.dto.payment.PaymentDto;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.modelmapper.ModelMapper;
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
    private PlanService planService;
    @Autowired
    private BusinessSchoolService businessSchoolService;


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
        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();

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
                // 小课训练营购买
                MonthlyCampOrder campOrder = signupService.getMonthlyCampOrder(orderId);
                if (campOrder == null) {
                    logger.error("{} 订单不存在", orderId);
                    return WebUtils.error(ErrorMessageUtils.getErrmsg("signup.fail"));
                } else {
                    entry = campOrder.getEntry();
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
                payService.risePaySuccess(orderId, monthlyCampConfig);
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

    @Deprecated
    @RequestMapping(value = "/info/load", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadInfo(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("提交个人信息")
                .action("加载个人信息");
        operationLogService.log(operationLog);
        Profile account = accountService.getProfile(loginUser.getId());
        ModelMapper modelMapper = new ModelMapper();
        InfoSubmitDto infoSubmitDto = modelMapper.map(account, InfoSubmitDto.class);
        return WebUtils.result(infoSubmitDto);
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

    @RequestMapping(value = "/rise/member", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getRiseMemberPayInfo(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("报名页面")
                .action("加载Rise会员信息");
        operationLogService.log(operationLog);

        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();
//        List<MemberType> memberTypesPayInfo = signupService.getMemberTypesPayInfo(monthlyCampConfig);
        // 查看优惠券信息
//        List<Coupon> coupons = signupService.getCoupons(loginUser.getId());
        RiseMember riseMember = signupService.currentRiseMember(loginUser.getId());
        RiseMemberDto dto = new RiseMemberDto();
//        dto.setMemberTypes(memberTypesPayInfo);

        if (riseMember != null && riseMember.getMemberTypeId() != null) {
            Integer memberTypeId = riseMember.getMemberTypeId();
            if (memberTypeId.equals(RiseMember.HALF) || memberTypeId.equals(RiseMember.ANNUAL)) {
                dto.setButtonStr("升级商学院");
            } else if (memberTypeId.equals(RiseMember.ELITE) || memberTypeId.equals(RiseMember.HALF_ELITE)) {
                dto.setButtonStr("入学商学院");
            } else {
                dto.setButtonStr("立即入学");
            }
        } else {
            dto.setButtonStr("立即入学");
        }
        AuditionClassMember classMember = planService.getAuditionClassMember(loginUser.getId());

        if (classMember != null) {
            // 有试听课
            dto.setAuditionStr("试听课");
        } else {
            // 没有试听课
            dto.setAuditionStr("预约试听");
        }

        Date dealTime = businessSchoolService.loadLastApplicationDealTime(loginUser.getId());
        calcDealTime(dealTime, dto, loginUser.getId());
        List<RiseMember> riseMembers = signupService.loadPersonalAllRiseMembers(loginUser.getId());
        // 用户层级是商学院用户或者层级是小课训练营用户，则不显示试听课入口
        Long count = riseMembers.stream()
                .filter(member -> member.getMemberTypeId() == RiseMember.ELITE || member.getMemberTypeId() == RiseMember.CAMP)
                .count();
        if (count > 0) {
            // 商学院和训练营不显示试听课按钮
            dto.setAuditionStr(null);
        }

        dto.setPrivilege(accountService.hasPrivilegeForBusinessSchool(loginUser.getId()));
//        dto.setCoupons(coupons);
        return WebUtils.result(dto);
    }

    private void calcDealTime(Date dealTime, RiseMemberDto dto, Integer profileId) {
        // 默认订单开放时间是48小时
        if(dealTime == null){
            dto.setRemainHour(48);
            dto.setRemainMinute(0);
        }else{
            int time = DateUtils.intervalMinute(DateUtils.afterHours(dealTime, 48));
            if(time<=0){
                businessSchoolService.expireApplication(profileId);
                dto.setRemainHour(0);
                dto.setRemainMinute(0);
            }else{
                dto.setRemainHour(time/60);
                dto.setRemainMinute(time % 60);
            }
        }
    }

    @RequestMapping(value = "/rise/member/check/{memberTypeId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> checkRiseMemberDate(LoginUser loginUser, @PathVariable Integer memberTypeId) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("报名页面")
                .action("点击RISE会员选择按钮")
                .memo(memberTypeId + "");
        operationLogService.log(operationLog);
        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();

        if (memberTypeId == RiseMember.ELITE) {
            boolean pass = accountService.hasPrivilegeForBusinessSchool(loginUser.getId());
            if (!pass) {
                return WebUtils.error(201, "请先提交申请");
            }
        }
        Pair<Integer, String> result = signupService.risePurchaseCheck(loginUser.getId(), memberTypeId, monthlyCampConfig);
        if (result.getLeft() != 1) {
            return WebUtils.error(result.getRight());
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
     * 检查用户权限，如果通过了，则返回GoodsType以及GoodsId
     */
    @RequestMapping(value = "/check/business/school/privilege", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> checkBusinessSchoolPrivilege(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后端")
                .function("商学院")
                .action("检查商学院报名权限");
        operationLogService.log(operationLog);
        // 检查状态
        Boolean check = accountService.hasPrivilegeForBusinessSchool(loginUser.getId());
        BusinessSchoolDto dto = new BusinessSchoolDto();
        dto.setPrivilege(check);
        RiseMember riseMember = signupService.currentRiseMember(loginUser.getId());
        if (riseMember == null) {
            // 其他 -1
            dto.setRiseMember(-1);
        } else {
            if (riseMember.getMemberTypeId() == RiseMember.ELITE || riseMember.getMemberTypeId() == RiseMember.HALF_ELITE) {
                // 精英版 1
                dto.setRiseMember(1);
            } else {
                // 非精英版 2
                dto.setRiseMember(2);
            }
        }
        return WebUtils.result(dto);
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
            logger.error("获取商品信息的商品类型异常,{}", goodsInfoDto);
            return WebUtils.error("商品类型异常");
        }
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后端")
                .function("报名页面")
                .action("获取商品信息")
                .memo(goodsInfoDto.getGoodsType());
        operationLogService.log(operationLog);

        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();

        // 是否能使用多个优惠券
        goodsInfoDto.setMultiCoupons(this.checkMultiCoupons(goodsInfoDto.getGoodsType()));
        // 计算价格/等特殊
        MemberType memberType = signupService.getMemberTypesPayInfo(loginUser.getId(), monthlyCampConfig).stream()
                .filter(item -> item.getId().equals(goodsInfoDto.getGoodsId()))
                .findFirst().orElse(null);
        if (memberType != null) {
            goodsInfoDto.setFee(memberType.getFee());
            goodsInfoDto.setStartTime(memberType.getStartTime());
            goodsInfoDto.setEndTime(memberType.getEndTime());
            goodsInfoDto.setInitPrice(memberType.getFee());
            goodsInfoDto.setName(memberType.getName());
        }

        BusinessSchool bs = signupService.getSchoolInfoForPay(loginUser.getId());
        if (QuanwaiOrder.FRAG_MEMBER.equals(goodsInfoDto.getGoodsType()) && !bs.getIsBusinessStudent()) {
            goodsInfoDto.setFee(bs.getFee());
        }

        // 获取优惠券
        List<Coupon> coupons = signupService.getCoupons(loginUser.getId());
        goodsInfoDto.setCoupons(coupons);
        // 自动选择优惠券
        List<Coupon> autoCoupons = this.autoChooseCoupon(goodsInfoDto, coupons);
        goodsInfoDto.setAutoCoupons(autoCoupons);
        return WebUtils.result(goodsInfoDto);
    }

    private List<Coupon> autoChooseCoupon(GoodsInfoDto goodsInfoDto, List<Coupon> coupons) {
        List<Coupon> list = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(coupons)) {
            // 有优惠券
            switch (goodsInfoDto.getGoodsType()) {
                case QuanwaiOrder.FRAG_MEMBER:
                    // 商学院--按照到期时间逆序排序，从上往下选，当支付金额为0时不再继续选择
                    coupons.sort((o1, o2) -> o1.getExpiredDate().after(o2.getExpiredDate()) ? 1 : -1);
                    Double total = 0d;
                    for (Coupon coupon : coupons) {
                        list.add(coupon);
                        total += coupon.getAmount();
                        if (total >= goodsInfoDto.getFee()) {
                            // 优惠券金额大于等于价格
                            break;
                        }
                    }
                    break;
                case QuanwaiOrder.FRAG_CAMP:
                    // 选择最大的一张
                    Coupon maxCoupon = coupons.stream().filter(item -> item.getCategory() == null).max((o1, o2) -> o1.getAmount() - o2.getAmount() > 0 ? 1 : -1).orElse(null);
                    list.add(maxCoupon);
                    break;
                default:
                    break;
            }
        }
        return list;
    }


    private Boolean checkMultiCoupons(String goodsType) {
        switch (goodsType) {
            case QuanwaiOrder.FRAG_MEMBER:
                return true;
            case QuanwaiOrder.FRAG_CAMP:
                return false;
            default:
                return false;
        }
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
            logger.error("获取商品信息的商品类型异常,{}", paymentDto);
            return WebUtils.error("商品类型异常");
        }

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后端")
                .function("报名页面")
                .action("点击支付")
                .memo(paymentDto.getGoodsType());
        operationLogService.log(operationLog);
        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();

        // 检查ip
        String remoteIp = request.getHeader("X-Forwarded-For");
        if (remoteIp == null) {
            remoteIp = ConfigUtils.getExternalIP();
        }

        Pair<Integer, String> check;
        // 检查是否能够支付
        switch (paymentDto.getGoodsType()) {
            case QuanwaiOrder.FRAG_MEMBER:
                // 会员购买
                check = signupService.risePurchaseCheck(loginUser.getId(), paymentDto.getGoodsId(), monthlyCampConfig);
                break;
            case QuanwaiOrder.FRAG_CAMP:
                // 小课训练营购买
                check = signupService.risePurchaseCheck(loginUser.getId(), paymentDto.getGoodsId(), monthlyCampConfig);
                break;
            default:
                check = new MutablePair<>(-1, "校验失败");
        }

        if (check.getLeft() != 1) {
            return WebUtils.error(check.getRight());
        }

        // 检查优惠券
        if (paymentDto.getCouponId() != null) {
            if (!costRepo.checkCouponValidation(loginUser.getId(), paymentDto.getCouponId())) {
                return WebUtils.error("该优惠券无效");
            }
        }
        if (CollectionUtils.isNotEmpty(paymentDto.getCouponsIdGroup())) {
            for (Integer coupon : paymentDto.getCouponsIdGroup()) {
                if (!costRepo.checkCouponValidation(loginUser.getId(), coupon)) {
                    return WebUtils.error("该优惠券无效");
                }
            }
        }

        // 根据前端传进来的 param 创建订单信息
        QuanwaiOrder quanwaiOrder = this.createQuanwaiOrder(paymentDto, loginUser.getId(), monthlyCampConfig);
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
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("报名")
                .function("报名页面")
                .action("计算优惠券减免");
        operationLogService.log(operationLog);
        Double price;
        switch (paymentDto.getGoodsType()) {
            case QuanwaiOrder.FRAG_MEMBER:
                price = signupService.calculateMemberCoupon(loginUser.getId(), paymentDto.getGoodsId(), paymentDto.getCouponsIdGroup());
                return WebUtils.result(price);
            case QuanwaiOrder.FRAG_CAMP:
                List<Integer> campCoupons = Lists.newArrayList(paymentDto.getCouponId());
                price = signupService.calculateMemberCoupon(loginUser.getId(), paymentDto.getGoodsId(), campCoupons);
                return WebUtils.result(price);
            default:
                logger.error("异常，用户:{}商品类型有问题:{}", loginUser.getId(), paymentDto);
                return WebUtils.error("商品类型异常");
        }
    }

    @RequestMapping(value = "/current/camp/month", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> validateCampUrl(LoginUser loginUser) {
        Assert.notNull(loginUser, "登录用户不能为空");
        MonthlyCampProcessDto dto = new MonthlyCampProcessDto();
        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();
        Integer currentCampMonth = signupService.loadCurrentCampMonth(monthlyCampConfig);
        dto.setMarKSellingMemo(monthlyCampConfig.getSellingYear() + "-" + monthlyCampConfig.getSellingMonth());
        dto.setCurrentCampMonth(currentCampMonth);
        dto.setCampMonthProblemId(signupService.loadHrefProblemId(currentCampMonth));
        return WebUtils.result(dto);
    }

    /**
     * 创建订单
     * @param paymentDto 支付信息
     * @param profileId 用户id
     * @return 订单对象
     */
    private QuanwaiOrder createQuanwaiOrder(PaymentDto paymentDto, Integer profileId, MonthlyCampConfig monthlyCampConfig) {
        switch (paymentDto.getGoodsType()) {
            case QuanwaiOrder.FRAG_MEMBER: {
                return signupService.signupRiseMember(profileId, paymentDto.getGoodsId(), paymentDto.getCouponsIdGroup());
            }
            case QuanwaiOrder.FRAG_CAMP: {
                return signupService.signupMonthlyCamp(profileId, paymentDto.getGoodsId(), paymentDto.getCouponId(), monthlyCampConfig);
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

    @RequestMapping("/rise/member/{memberTypeId}")
    public ResponseEntity<Map<String, Object>> riseMember(@PathVariable Integer memberTypeId, LoginUser loginUser) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("用户信息")
                .function("RISE")
                .action("查询rise会员信息")
                .memo(String.valueOf(memberTypeId));
        operationLogService.log(operationLog);

        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();

        RiseMember riseMember = null;

        switch (memberTypeId) {
            case RiseMember.ELITE:
                riseMember = signupService.getCurrentRiseMemberStatus(loginUser.getId(), monthlyCampConfig);
                break;
            case RiseMember.CAMP:
                riseMember = signupService.getCurrentMonthlyCampStatus(loginUser.getId(), monthlyCampConfig);
                break;
            default:
                break;
        }
        if (riseMember != null) {
            return WebUtils.result(riseMember.simple());
        } else {
            return WebUtils.error("会员类型校验出错");
        }
    }

    @RequestMapping("/rise/audition/button")
    public ResponseEntity<Map<String,Object>> loadAuditions(LoginUser loginUser){
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("用户信息")
                .function("RISE")
                .action("查询试听课信息");
        operationLogService.log(operationLog);
        AuditionClassMember classMember = planService.getAuditionClassMember(loginUser.getId());
        String auditionStr = "";
        if (classMember != null) {
            auditionStr = "试听课";
        }
        return WebUtils.result(auditionStr);
    }
}
