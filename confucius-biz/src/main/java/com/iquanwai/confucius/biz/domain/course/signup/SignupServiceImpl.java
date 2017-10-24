package com.iquanwai.confucius.biz.domain.course.signup;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.RedisUtil;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.course.ClassMemberDao;
import com.iquanwai.confucius.biz.dao.course.CouponDao;
import com.iquanwai.confucius.biz.dao.course.CourseOrderDao;
import com.iquanwai.confucius.biz.dao.fragmentation.*;
import com.iquanwai.confucius.biz.dao.wx.QuanwaiOrderDao;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.customer.CustomerMessageService;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.po.systematism.ClassMember;
import com.iquanwai.confucius.biz.po.systematism.CourseIntroduction;
import com.iquanwai.confucius.biz.po.systematism.CourseOrder;
import com.iquanwai.confucius.biz.po.systematism.QuanwaiClass;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQPublisher;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.ref.SoftReference;
import java.net.ConnectException;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    private CourseOrderDao courseOrderDao;
    @Autowired
    private CouponDao couponDao;
    @Autowired
    private QuanwaiOrderDao quanwaiOrderDao;
    @Autowired
    private ClassMemberCountRepo classMemberCountRepo;
    @Autowired
    private ClassMemberDao classMemberDao;
    @Autowired
    private MonthlyCampScheduleDao monthlyCampScheduleDao;
    @Autowired
    private CostRepo costRepo;
    @Autowired
    private RiseMemberTypeRepo riseMemberTypeRepo;
    @Autowired
    private RiseOrderDao riseOrderDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private MonthlyCampOrderDao monthlyCampOrderDao;
    @Autowired
    private MessageService messageService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    private int PROBLEM_MAX_LENGTH = 30; //小课最长开放时间

    private Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 小课训练营购买之后送的优惠券
     */
    private final static double MONTHLY_CAMP_COUPON = 100;

    private Map<Integer, SoftReference<QuanwaiClass>> classMap = Maps.newHashMap();
    private Map<Integer, CourseIntroduction> courseMap = Maps.newHashMap();

    private RabbitMQPublisher rabbitMQPublisher;
    private RabbitMQPublisher paySuccessPublisher;
    private RabbitMQPublisher freshLoginUserPublisher;

    /**
     * 初始化缓存
     */
    @PostConstruct
    public void init() {
        classMap.clear();
        courseMap.clear();
        paySuccessPublisher = rabbitMQFactory.initFanoutPublisher("rise_pay_success_topic");
        freshLoginUserPublisher = rabbitMQFactory.initFanoutPublisher("login_user_reload");
        rabbitMQPublisher = rabbitMQFactory.initFanoutPublisher("camp_order_topic");
    }


    @Override
    public Pair<Integer, String> risePurchaseCheck(Integer profileId, Integer memberTypeId, MonthlyCampConfig monthlyCampConfig) {
        Profile profile = accountService.getProfile(profileId);

        RiseMember riseMember = this.currentRiseMember(profileId);
        Assert.notNull(profile, "用户不能为空");
        Integer left = -1;
        String right = "正常";
        if (memberTypeId == RiseMember.ELITE) {
            // 购买会员
            if (profile.getRiseMember() == Constants.RISE_MEMBER.MEMBERSHIP &&
                    (RiseMember.HALF_ELITE == riseMember.getMemberTypeId() || RiseMember.ELITE == riseMember.getMemberTypeId())) {
                // right = "您已经是圈外商学院学员，无需重复报名\n如有疑问请在学习群咨询班长";
                left = 1;
            } else if (profile.getRiseMember() == Constants.RISE_MEMBER.MONTHLY_CAMP) {
                left = 1;
            } else {
                // 查看是否开放报名
                if (ConfigUtils.getRisePayStopTime().before(new Date())) {
                    right = "Hi，谢谢你关注【圈外同学】!\n不过...本次报名已达到限额了\n记得及时关注下期开放通知哦";
                } else {
                    left = 1;
                }
            }
        } else if (memberTypeId == RiseMember.CAMP) {
            // 购买小课训练营
            if (profile.getRiseMember() == Constants.RISE_MEMBER.MEMBERSHIP &&
                    (RiseMember.HALF_ELITE == riseMember.getMemberTypeId() || RiseMember.ELITE == riseMember.getMemberTypeId())) {
                right = "您已经是圈外商学院学员，拥有主题训练营，无需重复报名\n如有疑问请在学习群咨询班长";
            } else {
                if (profile.getRiseMember() == Constants.RISE_MEMBER.MONTHLY_CAMP) {
                    List<RiseClassMember> classMembers = riseClassMemberDao.queryByProfileId(profileId);
                    List<Integer> months = classMembers.stream().map(RiseClassMember::getMonth).collect(Collectors.toList());
                    if (months.contains(monthlyCampConfig.getSellingMonth())) {
                        right = "您已经是" + monthlyCampConfig.getSellingMonth() + "月小课训练营用户";
                    } else {
                        left = 1;
                    }
                } else if (!monthlyCampConfig.getPurchaseSwitch()) {
                    right = "当月小课训练营已关闭报名";
                } else {
                    left = 1;
                }
            }
        }
        return new MutablePair<>(left, right);
    }


    @Override
    public QuanwaiOrder signupRiseMember(Integer profileId, Integer memberTypeId, List<Integer> couponId) {
        // 查询该openid是否是我们的用户
        Profile profile = accountService.getProfile(profileId);
        MemberType memberType = riseMemberTypeRepo.memberType(memberTypeId);
        Double fee;
        BusinessSchool bs = this.getSchoolInfoForPay(profileId);
        if (memberTypeId == RiseMember.ELITE) {
            // 报名小课精英版
            fee = bs.getFee();
        } else {
            fee = memberType.getFee();
        }
        Pair<String, Double> orderPair = generateOrderId(fee, couponId);

        Assert.notNull(profile, "用户信息错误");
        Assert.notNull(memberType, "会员类型错误");
        QuanwaiOrder quanwaiOrder = this.createQuanwaiOrder(profile.getOpenid(),
                orderPair.getLeft(), fee, orderPair.getRight(), memberTypeId + "", memberType.getName(), QuanwaiOrder.FRAG_MEMBER);

        // rise的报名数据
        RiseOrder riseOrder = new RiseOrder();
        riseOrder.setOpenid(profile.getOpenid());
        riseOrder.setEntry(false);
        riseOrder.setIsDel(false);
        riseOrder.setMemberType(memberTypeId);
        riseOrder.setOrderId(orderPair.getLeft());
        riseOrder.setProfileId(profile.getId());
        riseOrderDao.insert(riseOrder);
        return quanwaiOrder;
    }

    @Override
    public QuanwaiOrder signupMonthlyCamp(Integer profileId, Integer memberTypeId, Integer couponId, MonthlyCampConfig monthlyCampConfig) {
        // 如果是购买训练营小课，配置 zk，查看当前月份
        Profile profile = accountService.getProfile(profileId);
        MemberType memberType = riseMemberTypeRepo.memberType(memberTypeId);
        Assert.notNull(profile, "用户不能为空");
        Assert.notNull(memberType, "会员类型错误");

        Double fee = memberType.getFee();
        Pair<String, Double> orderPair = generateOrderId(fee, couponId);

        QuanwaiOrder quanwaiOrder = createQuanwaiOrder(profile.getOpenid(),
                orderPair.getLeft(), fee, orderPair.getRight(),
                memberTypeId + "", monthlyCampConfig.getSellingMonth() + "月训练营", QuanwaiOrder.FRAG_CAMP);

        // 插入小课训练营报名数据
        MonthlyCampOrder monthlyCampOrder = new MonthlyCampOrder();
        monthlyCampOrder.setOrderId(orderPair.getLeft());
        monthlyCampOrder.setOpenId(profile.getOpenid());
        monthlyCampOrder.setProfileId(profileId);
        monthlyCampOrder.setMonth(monthlyCampConfig.getSellingMonth());
        monthlyCampOrderDao.insert(monthlyCampOrder);
        return quanwaiOrder;
    }

    @Override
    public ClassMember classMember(String orderId) {
        CourseOrder courseOrder = courseOrderDao.loadOrder(orderId);
        if (courseOrder != null) {
            return classMemberDao.getClassMember(courseOrder.getClassId(), courseOrder.getProfileId());
        }
        return null;
    }

    @Override
    public void payMonthlyCampSuccess(String orderId, MonthlyCampConfig monthlyCampConfig) {
        MonthlyCampOrder campOrder = monthlyCampOrderDao.loadCampOrder(orderId);
        Assert.notNull(campOrder, "训练营购买订单不能为空，orderId：" + orderId);
        Integer profileId = campOrder.getProfileId();
        // 更新 profile 表中状态
        Profile profile = accountService.getProfile(profileId);
        RiseMember existRiseMember = this.currentRiseMember(profileId);

        // RiseMember 新增记录
        String memberId = generateMemberId(monthlyCampConfig);
        RiseClassMember classMember = new RiseClassMember();
        classMember.setClassName(monthlyCampConfig.getCampClassPrefix());
        classMember.setMemberId(memberId);
        classMember.setProfileId(profileId);
        classMember.setYear(monthlyCampConfig.getSellingYear());
        classMember.setMonth(monthlyCampConfig.getSellingMonth());
        classMember.setActive(0);
        riseClassMemberDao.insert(classMember);

        // 每当在 RiseMember 表新增一种状态时候，预先在 RiseMember 表中其他数据置为过期
        if (existRiseMember == null) {
            // 添加会员表
            RiseMember riseMember = new RiseMember();
            riseMember.setOpenId(campOrder.getOpenId());
            riseMember.setOrderId(campOrder.getOrderId());
            riseMember.setProfileId(campOrder.getProfileId());
            riseMember.setMemberTypeId(RiseMember.CAMP);
            Date endDate = monthlyCampConfig.getCloseDate();
            riseMember.setExpireDate(endDate);
            riseMember.setExpired(false);
            riseMemberDao.insert(riseMember);
        } else {
            if (existRiseMember.getMemberTypeId() == RiseMember.ANNUAL
                    || existRiseMember.getMemberTypeId() == RiseMember.HALF
                    || existRiseMember.getMemberTypeId() == RiseMember.HALF_ELITE
                    || existRiseMember.getMemberTypeId() == RiseMember.ELITE) {
                // 如果当前购买的人的身份是商学院会员或者专业版会员，则直接将新增的数据记录置为过期
                // 添加会员表
                RiseMember riseMember = new RiseMember();
                riseMember.setOpenId(campOrder.getOpenId());
                riseMember.setOrderId(campOrder.getOrderId());
                riseMember.setProfileId(campOrder.getProfileId());
                riseMember.setMemberTypeId(RiseMember.CAMP);
                Date endDate = monthlyCampConfig.getCloseDate();
                riseMember.setExpireDate(endDate);
                riseMember.setExpired(true);
                riseMember.setMemo("专业版购买训练营");
                riseMemberDao.insert(riseMember);
            } else {
                riseMemberDao.updateExpiredAhead(profileId);
                // 添加会员表
                RiseMember riseMember = new RiseMember();
                riseMember.setOpenId(campOrder.getOpenId());
                riseMember.setOrderId(campOrder.getOrderId());
                riseMember.setProfileId(campOrder.getProfileId());
                riseMember.setMemberTypeId(RiseMember.CAMP);
                Date endDate = monthlyCampConfig.getCloseDate();
                riseMember.setExpireDate(endDate);
                riseMember.setExpired(false);
                riseMemberDao.insert(riseMember);
            }
        }

        // 送优惠券
        Coupon coupon = new Coupon();
        coupon.setOpenid(profile.getOpenid());
        coupon.setProfileId(profileId);
        coupon.setAmount(MONTHLY_CAMP_COUPON);
        coupon.setUsed(0);
        coupon.setExpiredDate(DateUtils.afterMonths(new Date(), 2));
        coupon.setCategory("ELITE_RISE_MEMBER");
        coupon.setDescription("会员抵用券");
        couponDao.insert(coupon);
        // 更新订单状态
        monthlyCampOrderDao.entry(orderId);

        // 发送 mq 消息，通知 platon 强行开启小课
        try {
            rabbitMQPublisher.publish(orderId);
        } catch (ConnectException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        try {
            // 休眠 3 秒
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        sendPurchaseMessage(profile, RiseMember.CAMP, orderId, monthlyCampConfig);
        // 刷新相关状态
        refreshStatus(quanwaiOrderDao.loadOrder(orderId), orderId);
    }

    @Override
    public MonthlyCampOrder getMonthlyCampOrder(String orderId) {
        return monthlyCampOrderDao.loadCampOrder(orderId);
    }

    /**
     * 生成 memberId，格式 YYYYMM + 6位数字
     */
    @Override
    public String generateMemberId(MonthlyCampConfig monthlyCampConfig) {
        StringBuilder targetMemberId = new StringBuilder();

        String prefix = monthlyCampConfig.getMemberIdPrefix();

        String key = "customer:memberId:" + prefix;
        redisUtil.lock("lock:memberId", (lock) -> {
            // TODO 有效期 60 天，期间 redis 绝对不能重启！！！
            String memberId = redisUtil.get(key);
            String sequence;
            if (StringUtils.isEmpty(memberId)) {
                sequence = "000001";
            } else {
                sequence = String.format("%06d", Integer.parseInt(memberId) + 1);
            }
            targetMemberId.append(prefix).append(sequence);
            redisUtil.set(key, sequence, DateUtils.afterDays(new Date(), 60).getTime());
        });
        return targetMemberId.toString();
    }

    @Override
    public void riseMemberEntry(String orderId, MonthlyCampConfig monthlyCampConfig) {
        RiseOrder riseOrder = riseOrderDao.loadOrder(orderId);

        try {
            RiseMember exist = riseMemberDao.loadByOrderId(orderId);
            if (riseOrder.getEntry() && exist != null && !exist.getExpired() && DateUtils.isSameDate(exist.getAddTime(), new Date())) {
                // 这个单子已经成功，且已经插入了riseMember，并且未过期,并且是今天的
                messageService.sendAlarm("报名模块次级异常", "微信多次回调",
                        "中", "订单id:" + orderId, "再次处理今天已经插入的risemember");
                return;
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        riseOrderDao.entry(orderId);
        String openId = riseOrder.getOpenid();
        MemberType memberType = riseMemberTypeRepo.memberType(riseOrder.getMemberType());
        if (RiseMember.ELITE == memberType.getId()) {
            //查看有没有老的
            //精英会员一年
            // RiseClassMember 新增会员记录
            String memberId = generateMemberId(monthlyCampConfig);
            RiseClassMember classMember = new RiseClassMember();
            classMember.setClassName(monthlyCampConfig.getRiseClassPrefix());
            classMember.setMemberId(memberId);
            classMember.setProfileId(riseOrder.getProfileId());
            classMember.setYear(monthlyCampConfig.getSellingYear());
            classMember.setMonth(monthlyCampConfig.getSellingMonth());
            classMember.setActive(0);
            riseClassMemberDao.insert(classMember);
        } else {
            logger.error("该会员ID异常{}", memberType);
            messageService.sendAlarm("报名模块出错", "会员id异常",
                    "高", "订单id:" + orderId, "会员类型异常");
            return;
        }
        // 查看是否存在现成会员数据
        RiseMember existRiseMember = riseMemberDao.loadValidRiseMember(riseOrder.getProfileId());
        // 如果存在，则将已经存在的 riseMember 数据置为已过期
        riseMemberDao.updateExpiredAhead(riseOrder.getProfileId());
        // 添加会员表
        RiseMember riseMember = new RiseMember();
        riseMember.setOpenId(riseOrder.getOpenid());
        riseMember.setOrderId(riseOrder.getOrderId());
        riseMember.setProfileId(riseOrder.getProfileId());
        riseMember.setMemberTypeId(memberType.getId());
        if (existRiseMember != null && (existRiseMember.getMemberTypeId().equals(RiseMember.ELITE) || existRiseMember.getMemberTypeId().equals(RiseMember.HALF_ELITE))) {
            riseMember.setExpireDate(DateUtils.afterNatureMonths(existRiseMember.getExpireDate(), 12));
        } else {
            riseMember.setExpireDate(DateUtils.afterNatureMonths(new Date(), 12));
        }
        riseMember.setExpired(false);
        riseMemberDao.insert(riseMember);

        // 所有计划设置为会员
        List<ImprovementPlan> plans = improvementPlanDao.loadUserPlans(riseOrder.getOpenid());
        // 不是会员的计划，设置一下
        // 给精英版正在进行的planid+1个求点评次数
        // 非精英版或者不是正在进行的，不加点评次数
        plans.stream().filter(plan -> !plan.getRiseMember()).forEach(plan -> {
            // 不是会员的计划，设置一下
            plan.setCloseDate(DateUtils.afterDays(new Date(), PROBLEM_MAX_LENGTH));
            if (plan.getStatus().equals(1) && (memberType.getId().equals(3) || memberType.getId().equals(4))) {
                // 给精英版正在进行的planid+1个求点评次数
                improvementPlanDao.becomeRiseEliteMember(plan);
            } else {
                // 非精英版或者不是正在进行的，不加点评次数
                improvementPlanDao.becomeRiseMember(plan);
            }
        });
        Profile profile = accountService.getProfile(openId, false);
        // 发送模板消息
        sendPurchaseMessage(profile, memberType.getId(), orderId, monthlyCampConfig);
    }

    private void sendPurchaseMessage(Profile profile, Integer memberTypeId, String orderId, MonthlyCampConfig monthlyCampConfig) {
        Assert.notNull(profile, "openid不能为空");
        logger.info("发送欢迎消息给付费用户{}", profile.getOpenid());
        boolean isFull = profile.getIsFull() == 1;
        boolean isBindMobile = profile.getMobileNo() != null;
        String detailUrl = ConfigUtils.domainName() + "/rise/static/customer/profile?goRise=true";
        String mobileUrl = ConfigUtils.domainName() + "/rise/static/customer/mobile/check?goRise=true";
        String sendUrl = isFull ? isBindMobile ? null : mobileUrl : detailUrl;

        switch (memberTypeId) {
            case RiseMember.ELITE: {
                logger.info("发送会员数据");
                // 发送消息给一年精英版的用户
                customerMessageService.sendCustomerMessage(profile.getOpenid(), ConfigUtils.getValue("pay.success.send.image"), Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
                customerMessageService.sendCustomerMessage(profile.getOpenid(), monthlyCampConfig.getRiseEntryKey(), Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                if (sendUrl != null) {
                    messageService.sendMessage("点此完善个人信息，才能参加校友会，获取更多人脉资源喔！", Objects.toString(profile.getId()), MessageService.SYSTEM_MESSAGE, sendUrl);
                }
                break;
            }
            case RiseMember.CAMP: {
                logger.info("发送小课训练营数据");
                // 发送消息给小课训练营购买用户
                customerMessageService.sendCustomerMessage(profile.getOpenid(), ConfigUtils.getValue("pay.success.send.image"), Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
                customerMessageService.sendCustomerMessage(profile.getOpenid(), monthlyCampConfig.getCampEntryKey(), Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                if (sendUrl != null) {
                    messageService.sendMessage("点此完善个人信息，才能参加校友会，获取更多人脉资源喔！", Objects.toString(profile.getId()), MessageService.SYSTEM_MESSAGE, sendUrl);
                }
                break;
            }
            default: {
                messageService.sendAlarm("报名模块出错", "报名后发送消息", "中", "订单id:" + orderId + "\nprofileId:" + profile.getId(), "会员类型异常");
            }
        }
    }

    @Override
    public void reloadClass() {
        init();
        //初始化班级剩余人数
        classMemberCountRepo.initClass();
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
    public MemberType getMemberType(Integer memberType) {
        return riseMemberTypeRepo.memberType(memberType);
    }

    @Override
    public List<Coupon> getCoupons(Integer profileId) {
        if (costRepo.hasCoupon(profileId)) {
            List<Coupon> coupons = costRepo.getCoupons(profileId);
            coupons.forEach(item -> item.setExpired(DateUtils.parseDateToStringByCommon(item.getExpiredDate())));
            return coupons;
        }
        return Lists.newArrayList();
    }

    @Override
    public List<MemberType> getMemberTypesPayInfo(MonthlyCampConfig monthlyCampConfig) {
        List<MemberType> memberTypes = riseMemberTypeRepo.memberTypes();
        // 写入会员开始和结束时间
        memberTypes.forEach(item -> {
            item.setStartTime(DateUtils.parseDateToStringByCommon(new Date()));
            if (item.getId().equals(RiseMember.CAMP)) {
                item.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(monthlyCampConfig.getCloseDate(), 1)));
            } else {
                item.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(DateUtils.afterNatureMonths(new Date(), item.getOpenMonth()), 1)));
            }
        });
        return memberTypes;
    }

    @Override
    public List<MemberType> getMemberTypesPayInfo(Integer profileId, MonthlyCampConfig monthlyCampConfig) {
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);

        List<MemberType> memberTypes = riseMemberTypeRepo.memberTypes();
        // 写入会员开始和结束时间
        for (MemberType memberType : memberTypes) {
            if (memberType.getId().equals(RiseMember.CAMP)) {
                // 小课训练营类型
                memberType.setStartTime(DateUtils.parseDateToStringByCommon(new Date()));
                memberType.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(monthlyCampConfig.getCloseDate(), 1)));
            } else if (memberType.getId().equals(RiseMember.ELITE) || memberType.getId().equals(RiseMember.HALF_ELITE)) {
                // 商学院类型（一年、半年）
                if (riseMember != null && (riseMember.getMemberTypeId().equals(RiseMember.ELITE) || riseMember.getMemberTypeId().equals(RiseMember.HALF_ELITE))) {
                    // 此时用户类型是 商学院会员
                    memberType.setStartTime(DateUtils.parseDateToStringByCommon(riseMember.getExpireDate()));
                    memberType.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(DateUtils.afterNatureMonths(riseMember.getExpireDate(), memberType.getOpenMonth()), 1)));
                } else {
                    // 非商学院会员
                    memberType.setStartTime(DateUtils.parseDateToStringByCommon(new Date()));
                    memberType.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(DateUtils.afterNatureMonths(new Date(), memberType.getOpenMonth()), 1)));
                }
            } else {
                memberType.setStartTime(DateUtils.parseDateToStringByCommon(new Date()));
                memberType.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(DateUtils.afterNatureMonths(new Date(), memberType.getOpenMonth()), 1)));
            }
        }
        return memberTypes;
    }

    @Override
    public Double calculateMemberCoupon(Integer profileId, Integer memberTypeId, List<Integer> couponIdGroup) {
        Double amount = couponIdGroup.stream().map(costRepo::getCoupon).filter(Objects::nonNull).mapToDouble(Coupon::getAmount).sum();
        MemberType memberType = riseMemberTypeRepo.memberType(memberTypeId);
        Double fee;
        BusinessSchool bs = this.getSchoolInfoForPay(profileId);
        if (memberTypeId == RiseMember.ELITE) {
            // 报名小课精英版
            fee = bs.getFee();
        } else {
            fee = memberType.getFee();
        }
        if (fee >= amount) {
            return CommonUtils.substract(fee, amount);
        } else {
            return 0D;
        }
    }


    @Override
    public RiseMember currentRiseMember(Integer profileId) {
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        if (riseMember != null) {
            riseMember.setStartTime(DateUtils.parseDateToStringByCommon(riseMember.getAddTime()));
            riseMember.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(riseMember.getExpireDate(), 1)));
        }
        return riseMember;
    }

    @Override
    public Integer loadCurrentCampMonth(MonthlyCampConfig monthlyCampConfig) {
        return monthlyCampConfig.getSellingMonth();
    }

    /**
     * 小课售卖页面，跳转小课介绍页面 problemId
     */
    @Override
    public Integer loadHrefProblemId(Integer month) {
        List<MonthlyCampSchedule> schedules = monthlyCampScheduleDao.loadByMonth(month);
        MonthlyCampSchedule schedule = schedules.stream().findFirst().get();
        return schedule.getProblemId();
    }


    @Override
    public BusinessSchool getSchoolInfoForPay(Integer profileId) {
        BusinessSchool businessSchool = new BusinessSchool();
        Double fee;
        RiseMember riseMember = this.currentRiseMember(profileId);
        MemberType memberType = this.getMemberType(RiseMember.ELITE);
        businessSchool.setIsBusinessStudent(false);
        if (riseMember != null) {
            switch (riseMember.getMemberTypeId()) {
                case RiseMember.ELITE:
                case RiseMember.HALF_ELITE:
                    fee = memberType.getFee();
                    businessSchool.setIsBusinessStudent(true);
                    break;
                case RiseMember.ANNUAL:
                    fee = CommonUtils.substract(memberType.getFee(), 880d);
                    break;
                case RiseMember.HALF:
                    fee = CommonUtils.substract(memberType.getFee(), 580d);
                    break;
                case RiseMember.CAMP:
                    fee = memberType.getFee();
                    break;
                default:
                    fee = memberType.getFee();
            }

        } else {
            fee = memberType.getFee();
        }
        if (!ConfigUtils.reducePriceForNotElite()) {
            // 关闭减价，恢复原价
            fee = memberType.getFee();
        }

        businessSchool.setFee(fee);
        return businessSchool;
    }

    @Override
    public RiseMember getCurrentRiseMemberStatus(Integer profileId, MonthlyCampConfig monthlyCampConfig) {
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        if (riseMember.getMemberTypeId().equals(RiseMember.ELITE)) {
            riseMember.setStartTime(DateUtils.parseDateToStringByCommon(DateUtils.afterNatureMonths(riseMember.getExpireDate(), -12)));
        } else {
            riseMember.setStartTime(DateUtils.parseDateToStringByCommon(new Date()));
        }
        riseMember.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(riseMember.getExpireDate(), 1)));
        riseMember.setEntryCode(monthlyCampConfig.getRiseEntryKey());
        return riseMember;
    }

    @Override
    public RiseMember getCurrentMonthlyCampStatus(MonthlyCampConfig monthlyCampConfig) {
        RiseMember riseMember = new RiseMember();
        riseMember.setStartTime(DateUtils.parseDateToStringByCommon(new Date()));
        riseMember.setEndTime(DateUtils.parseDateToStringByCommon(monthlyCampConfig.getCloseDate()));
        riseMember.setEntryCode(monthlyCampConfig.getCampEntryKey());
        return riseMember;
    }


    private void refreshStatus(QuanwaiOrder quanwaiOrder, String orderId) {
        // 刷新会员状态
        try {
            freshLoginUserPublisher.publish(quanwaiOrder.getOpenid());
        } catch (ConnectException e) {
            logger.error("发送会员信息更新mq失败", e);
        }
        // 更新优惠券使用状态
        if (quanwaiOrder.getDiscount() != 0.0) {
            logger.info("{}使用优惠券", quanwaiOrder.getOpenid());
            costRepo.updateCoupon(Coupon.USED, orderId);
        }
        // 发送支付成功 mq 消息
        try {
            logger.info("发送支付成功message:{}", quanwaiOrder);
            paySuccessPublisher.publish(quanwaiOrder);
        } catch (ConnectException e) {
            logger.error("发送支付成功mq失败", e);
            messageService.sendAlarm("报名模块出错", "发送支付成功mq失败",
                    "高", "订单id:" + orderId, e.getLocalizedMessage());
        }
    }

    /**
     * 生成orderId以及计算优惠价格
     * @param fee 总价格
     * @param couponId 优惠券id 如果
     */
    private Pair<String, Double> generateOrderId(Double fee, Integer couponId) {
        //orderId 16位随机字符
        String orderId = CommonUtils.randomString(16);
        Double discount = 0d;
        if (couponId != null) {
            // 计算优惠
            Coupon coupon = costRepo.getCoupon(couponId);
            Assert.notNull(coupon, "优惠券无效");
            discount = costRepo.discount(fee, orderId, coupon);
        }
        return new MutablePair<>(orderId, discount);
    }

    /**
     * 生成orderId以及计算优惠价格
     * @param fee 总价格
     * @param couponIdGroup 优惠券id 如果
     */
    private Pair<String, Double> generateOrderId(Double fee, List<Integer> couponIdGroup) {
        //orderId 16位随机字符
        String orderId = CommonUtils.randomString(16);
        Double discount = 0d;
        if (CollectionUtils.isNotEmpty(couponIdGroup)) {
            // 计算优惠
            List<Coupon> coupons = couponIdGroup.stream().map(costRepo::getCoupon).collect(Collectors.toList());
            Assert.notEmpty(coupons, "优惠券无效");
            discount = costRepo.discount(fee, orderId, coupons);
        }
        return new MutablePair<>(orderId, discount);
    }

    private QuanwaiOrder createQuanwaiOrder(String openId, String orderId, Double fee, Double discount, String goodsId,
                                            String goodsName, String goodsType) {
        // 创建订单
        QuanwaiOrder quanwaiOrder = new QuanwaiOrder();
        quanwaiOrder.setCreateTime(new Date());
        quanwaiOrder.setOpenid(openId);
        quanwaiOrder.setOrderId(orderId);
        quanwaiOrder.setTotal(fee);
        quanwaiOrder.setDiscount(discount);
        quanwaiOrder.setPrice(CommonUtils.substract(fee, discount));
        quanwaiOrder.setStatus(QuanwaiOrder.UNDER_PAY);
        quanwaiOrder.setGoodsId(goodsId);
        quanwaiOrder.setGoodsName(goodsName);
        quanwaiOrder.setGoodsType(goodsType);
        quanwaiOrderDao.insert(quanwaiOrder);
        return quanwaiOrder;
    }

}
