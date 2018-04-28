package com.iquanwai.confucius.biz.domain.course.signup;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.RedisUtil;
import com.iquanwai.confucius.biz.dao.fragmentation.*;
import com.iquanwai.confucius.biz.dao.wx.QuanwaiOrderDao;
import com.iquanwai.confucius.biz.domain.fragmentation.CacheService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.po.fragmentation.course.CourseConfig;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/9/10.
 */
@Service
public class SignupServiceImpl implements SignupService {

    @Autowired
    private RiseClassMemberDao riseClassMemberDao;
    @Autowired
    private QuanwaiOrderDao quanwaiOrderDao;
    @Autowired
    private CostManger costManger;
    @Autowired
    private MemberTypeManager memberTypeManager;
    @Autowired
    private RiseOrderDao riseOrderDao;
    @Autowired
    private MonthlyCampOrderDao monthlyCampOrderDao;
    @Autowired
    private CourseScheduleDefaultDao courseScheduleDefaultDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private BusinessSchoolApplicationOrderDao businessSchoolApplicationOrderDao;
    @Autowired
    private RiseMemberManager riseMemberManager;

    @Override
    public Pair<Boolean, String> risePurchaseCheck(Integer profileId, Integer memberTypeId) {
        Integer number = redisUtil.getInt(SIGNUP_REMAIN_NUMBER_PREFIX + memberTypeId);
        if (number != null && number <= 0) {
            // 不能报名
            return Pair.of(false, "名额已满,请关注下期报名");
        }

        Pair<Boolean, String> pass = Pair.of(false, "类型异常");
        if (RiseMember.isApply(memberTypeId)) {
            Integer wannaMemberTypeId = riseMemberManager.loadWannaGoodsIdByApplyId(memberTypeId).getRight();
            pass = accountService.hasPrivilegeForApply(profileId, wannaMemberTypeId);
        } else if (RiseMember.isMember(memberTypeId)) {
            pass = accountService.hasPrivilegeForMember(profileId, memberTypeId);
        } else if (RiseMember.CAMP == memberTypeId) {
            pass = accountService.hasPrivilegeForCamp(profileId);
        }

        // TODO DELETE 预售阶段只有这几个班可以买
        RiseClassMember riseClassMember = riseClassMemberDao.whiteList(profileId);
        if (riseClassMember == null) {
            // 不能报名
            return Pair.of(false, "暂时只对部分用户预售");
        }

        return pass;
    }


    @Override
    public QuanwaiOrder signUpRiseMember(Integer profileId, Integer memberTypeId, List<Integer> couponId, Integer payType) {
        MemberType memberType = this.getMemberTypePayInfo(profileId, memberTypeId);
        Double fee = memberType.getFee();
        Pair<String, Double> orderPair = generateOrderId(fee, couponId);
        Assert.notNull(memberType, "会员类型错误");
        Assert.notNull(payType, "支付类型错误");
        QuanwaiOrder quanwaiOrder = this.createQuanwaiOrder(profileId, orderPair.getLeft(), fee, orderPair.getRight(),
                memberTypeId + "", memberType.getName(), QuanwaiOrder.FRAG_MEMBER, payType);

        // rise的报名数据
        RiseOrder riseOrder = new RiseOrder();
        riseOrder.setEntry(false);
        riseOrder.setIsDel(false);
        riseOrder.setMemberType(memberTypeId);
        riseOrder.setOrderId(orderPair.getLeft());
        riseOrder.setProfileId(profileId);
        riseOrderDao.insert(riseOrder);
        return quanwaiOrder;
    }


    @Override
    public QuanwaiOrder signUpMonthlyCamp(Integer profileId, Integer memberTypeId, Integer couponId, Integer payType) {
        // 如果是购买专项课，配置 zk，查看当前月份
        MemberType memberType = memberTypeManager.memberType(memberTypeId);
        Assert.notNull(memberType, "会员类型错误");

        CourseConfig monthlyCampConfig = cacheService.loadCourseConfig(RiseMember.CAMP);
        int sellingMonth = monthlyCampConfig.getSellingMonth();
        int sellingYear = monthlyCampConfig.getSellingYear();
        Double fee = memberType.getFee();
        Pair<String, Double> orderPair = generateOrderId(fee, couponId);

        QuanwaiOrder quanwaiOrder = createQuanwaiOrder(profileId, orderPair.getLeft(), fee, orderPair.getRight(),
                memberTypeId + "", sellingMonth + "月专项课", QuanwaiOrder.FRAG_CAMP, payType);

        // 插入专项课报名数据
        MonthlyCampOrder monthlyCampOrder = new MonthlyCampOrder();
        monthlyCampOrder.setOrderId(orderPair.getLeft());
        monthlyCampOrder.setProfileId(profileId);
        monthlyCampOrder.setYear(sellingYear);
        monthlyCampOrder.setMonth(sellingMonth);
        monthlyCampOrderDao.insert(monthlyCampOrder);
        return quanwaiOrder;
    }

    @Override
    public QuanwaiOrder signupBusinessSchoolApplication(Integer profileId, Integer memberTypeId, Integer couponId, Integer payType) {
        // 如果是购买专项课，配置 zk，查看当前月份
        MemberType memberType = this.getMemberTypePayInfo(profileId, memberTypeId);
        Assert.notNull(memberType, "会员类型错误");

        Double fee = memberType.getFee();
        Pair<String, Double> orderPair = generateOrderId(fee, couponId);

        QuanwaiOrder quanwaiOrder = createQuanwaiOrder(profileId,
                orderPair.getLeft(), fee, orderPair.getRight(),
                memberTypeId + "", "商学院申请", QuanwaiOrder.BS_APPLICATION, payType);

        // 插入专项课报名数据
        BusinessSchoolApplicationOrder bsOrder = new BusinessSchoolApplicationOrder();
        bsOrder.setOrderId(orderPair.getLeft());
        bsOrder.setProfileId(profileId);
        businessSchoolApplicationOrderDao.insert(bsOrder);
        return quanwaiOrder;
    }


    @Override
    public MonthlyCampOrder getMonthlyCampOrder(String orderId) {
        return monthlyCampOrderDao.loadCampOrder(orderId);
    }

    @Override
    public QuanwaiOrder getQuanwaiOrder(String orderId) {
        return quanwaiOrderDao.loadOrder(orderId);
    }

    @Override
    public RiseOrder getRiseOrder(String orderId) {
        return riseOrderDao.loadOrder(orderId);
    }

    @Override
    public List<Coupon> getCoupons(Integer profileId) {
        if (costManger.hasCoupon(profileId)) {
            List<Coupon> coupons = costManger.getCoupons(profileId);
            coupons.forEach(item -> item.setExpired(DateUtils.parseDateToStringByCommon(item.getExpiredDate())));
            return coupons;
        }
        return Lists.newArrayList();
    }

    @Override
    public MemberType getMemberTypePayInfo(Integer profileId, Integer memberTypeId) {
        // TODO 获得针对这个人的真实的价格，可针对不同人实现不同价格
        CourseConfig courseConfig = cacheService.loadCourseConfig(memberTypeId);

        MemberType memberType = memberTypeManager.memberTypes().stream().filter(item -> item.getId().equals(memberTypeId)).findFirst().orElse(null);
        if (memberType != null) {
            // 写入会员开始和结束时间
            if (courseConfig != null) {
                if (courseConfig.getCloseDate() != null) {
                    // 专项课类型 查询课程表，而不是MemberType里的
                    memberType.setStartTime(DateUtils.parseDateToStringByCommon(courseConfig.getOpenDate()));
                    memberType.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(courseConfig.getCloseDate(), 1)));
                } else {
                    memberType.setStartTime(DateUtils.parseDateToStringByCommon(courseConfig.getOpenDate()));
                    memberType.setEndTime(DateUtils.parseDateToStringByCommon(
                            DateUtils.beforeDays(DateUtils.afterMonths(courseConfig.getOpenDate(), memberType.getOpenMonth()), 1)));
                }
            }
        }
        return memberType;
    }

    @Override
    public Double calculateMemberCoupon(Integer profileId, Integer memberTypeId, List<Integer> couponIdGroup) {
        Double amount = couponIdGroup.stream().map(costManger::getCoupon).filter(Objects::nonNull).mapToDouble(Coupon::getAmount).sum();
        MemberType memberType = this.getMemberTypePayInfo(profileId, memberTypeId);
        Double fee = memberType.getFee();
        if (fee >= amount) {
            return CommonUtils.substract(fee, amount);
        } else {
            return 0D;
        }
    }

    /**
     * 课程售卖页面，跳转课程介绍页面 problemId
     */
    @Override
    public Integer loadHrefProblemId(Integer profileId, Integer month) {
        Integer category = accountService.loadUserScheduleCategory(profileId);
        List<CourseScheduleDefault> courseScheduleDefaults = courseScheduleDefaultDao.loadMajorCourseScheduleDefaultByCategory(category);

        return courseScheduleDefaults.stream()
                .filter(scheduleDefault -> month.equals(scheduleDefault.getMonth()))
                .map(CourseScheduleDefault::getProblemId)
                .findAny().orElse(null);
    }


    @Override
    public BusinessSchoolApplicationOrder getBusinessSchoolOrder(String orderId) {
        return businessSchoolApplicationOrderDao.loadBusinessSchoolApplicationOrder(orderId);
    }

    /**
     * 生成orderId以及计算优惠价格
     *
     * @param fee      总价格
     * @param couponId 优惠券id 如果
     */
    private Pair<String, Double> generateOrderId(Double fee, Integer couponId) {
        //orderId 16位随机字符
        String orderId = CommonUtils.randomString(16);
        Double discount = 0d;
        if (couponId != null) {
            // 计算优惠
            Coupon coupon = costManger.getCoupon(couponId);
            Assert.notNull(coupon, "优惠券无效");
            discount = costManger.discount(fee, orderId, coupon);
        }
        return Pair.of(orderId, discount);
    }

    /**
     * 生成orderId以及计算优惠价格
     *
     * @param fee           总价格
     * @param couponIdGroup 优惠券id 如果
     */
    private Pair<String, Double> generateOrderId(Double fee, List<Integer> couponIdGroup) {
        //orderId 16位随机字符
        String orderId = CommonUtils.randomString(16);
        Double discount = 0d;
        if (CollectionUtils.isNotEmpty(couponIdGroup)) {
            // 计算优惠
            List<Coupon> coupons = couponIdGroup.stream().map(costManger::getCoupon).collect(Collectors.toList());
            Assert.notEmpty(coupons, "优惠券无效");
            discount = costManger.discount(fee, orderId, coupons);
        }
        return Pair.of(orderId, discount);
    }

    private QuanwaiOrder createQuanwaiOrder(Integer profileId, String orderId, Double fee, Double discount, String goodsId,
                                            String goodsName, String goodsType, Integer payType) {
        // 创建订单
        QuanwaiOrder quanwaiOrder = new QuanwaiOrder();
        quanwaiOrder.setCreateTime(new Date());
        quanwaiOrder.setProfileId(profileId);
        quanwaiOrder.setOrderId(orderId);
        quanwaiOrder.setTotal(fee);
        quanwaiOrder.setDiscount(discount);
        quanwaiOrder.setPrice(CommonUtils.substract(fee, discount));
        quanwaiOrder.setStatus(QuanwaiOrder.UNDER_PAY);
        quanwaiOrder.setGoodsId(goodsId);
        quanwaiOrder.setGoodsName(goodsName);
        quanwaiOrder.setGoodsType(goodsType);
        quanwaiOrder.setPayType(payType);
        quanwaiOrderDao.insert(quanwaiOrder);
        return quanwaiOrder;
    }

    @Override
    public List<Coupon> autoChooseCoupon(String goodsType, Double fee, List<Coupon> coupons) {
        List<Coupon> list = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(coupons)) {
            // 有优惠券
            switch (goodsType) {
                case QuanwaiOrder.FRAG_MEMBER:
                    // 商学院--按照到期时间逆序排序，从上往下选，当支付金额为0时不再继续选择
                    coupons.sort((o1, o2) -> o1.getExpiredDate().after(o2.getExpiredDate()) ? 1 : -1);
                    Double total = 0d;
                    for (Coupon coupon : coupons) {
                        list.add(coupon);
                        total += coupon.getAmount();
                        if (total >= fee) {
                            // 优惠券金额大于等于价格
                            break;
                        }
                    }
                    break;
                case QuanwaiOrder.FRAG_CAMP:
                    // 选择最大的一张
                    Coupon maxCoupon = coupons.stream()
                            .filter(item -> item.getCategory() == null)
                            .max((o1, o2) -> o1.getAmount() - o2.getAmount() > 0 ? 1 : -1)
                            .orElse(null);
                    if (maxCoupon != null) {
                        list.add(maxCoupon);
                    }
                    break;
                default:
                    break;
            }
        }
        return list;
    }

    @Override
    public String getSubscribeQrCodeForPay(Integer memberTypeId) {
        String qrCodeUrl = "";
        switch (memberTypeId) {
            case RiseMember.BS_APPLICATION:
                qrCodeUrl = ConfigUtils.getCoreApplyQrCode();
                break;
            case RiseMember.BUSINESS_THOUGHT_APPLY:
                qrCodeUrl = ConfigUtils.getBusinessThoughtApplyQrCode();
                break;
            default:
                break;
        }
        return qrCodeUrl;
    }

    @Override
    public void changeRemainNumber(Integer remainNumber, Integer memberTypeId) {
        if (remainNumber == null) {
            redisUtil.deleteByPattern(SIGNUP_REMAIN_NUMBER_PREFIX + memberTypeId);
        } else {
            redisUtil.set(SIGNUP_REMAIN_NUMBER_PREFIX + memberTypeId, remainNumber, TimeUnit.DAYS.toSeconds(7));
        }
    }


}
