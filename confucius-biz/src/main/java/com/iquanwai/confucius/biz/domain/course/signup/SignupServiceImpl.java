package com.iquanwai.confucius.biz.domain.course.signup;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.RedisUtil;
import com.iquanwai.confucius.biz.dao.apply.BusinessSchoolApplicationDao;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.course.CouponDao;
import com.iquanwai.confucius.biz.dao.fragmentation.BusinessSchoolApplicationOrderDao;
import com.iquanwai.confucius.biz.dao.fragmentation.CourseScheduleDefaultDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.confucius.biz.dao.fragmentation.MonthlyCampOrderDao;
import com.iquanwai.confucius.biz.dao.fragmentation.OperateRotateDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseClassMemberDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseOrderDao;
import com.iquanwai.confucius.biz.dao.wx.QuanwaiOrderDao;
import com.iquanwai.confucius.biz.domain.fragmentation.CacheService;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.customer.CustomerMessageService;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.OperateRotate;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.BusinessSchoolApplicationOrder;
import com.iquanwai.confucius.biz.po.fragmentation.BusinessSchoolConfig;
import com.iquanwai.confucius.biz.po.fragmentation.CourseScheduleDefault;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampConfig;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampOrder;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.po.fragmentation.RiseOrder;
import com.iquanwai.confucius.biz.po.systematism.CourseIntroduction;
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

import javax.annotation.PostConstruct;
import java.lang.ref.SoftReference;
import java.net.ConnectException;
import java.util.Comparator;
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
    private CouponDao couponDao;
    @Autowired
    private QuanwaiOrderDao quanwaiOrderDao;
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
    private OperateRotateDao operateRotateDao;
    @Autowired
    private CourseScheduleDefaultDao courseScheduleDefaultDao;
    @Autowired
    private MessageService messageService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private BusinessSchoolApplicationOrderDao businessSchoolApplicationOrderDao;
    @Autowired
    private BusinessSchoolApplicationDao businessSchoolApplicationDao;

    private final static int PROBLEM_MAX_LENGTH = 30; //课程最长开放时间

    private Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 训练营购买之后送的优惠券
     */
    private final static double MONTHLY_CAMP_COUPON = 100;

    private Map<Integer, SoftReference<QuanwaiClass>> classMap = Maps.newHashMap();
    private Map<Integer, CourseIntroduction> courseMap = Maps.newHashMap();

    private RabbitMQPublisher rabbitMQPublisher;
    private RabbitMQPublisher paySuccessPublisher;
    private RabbitMQPublisher freshLoginUserPublisher;
    private RabbitMQPublisher openProblemPublisher;

    private static final String RISEMEMBER_OPERATEROTATE_SCENE_CODE = "rise_member_pay_success";
    private static final String MONTHLYCAMP_OPERATEROTATE_SCENE_CODE = "monthly_camp_pay_success";
    private static final int OPERATEROTATE_SWITCH_SIZE = 200;

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
        openProblemPublisher = rabbitMQFactory.initFanoutPublisher("monthly_camp_force_open_topic");
    }

    @Override
    public Pair<Integer, String> risePurchaseCheck(Integer profileId, Integer memberTypeId) {
        Profile profile = accountService.getProfile(profileId);

        RiseMember riseMember = this.currentRiseMember(profileId);
        Assert.notNull(profile, "用户不能为空");
        Integer left = -1;
        String right = "正常";
        if (memberTypeId == RiseMember.ELITE) {
            // 购买会员
            BusinessSchoolConfig businessSchoolConfig = cacheService.loadBusinessCollegeConfig();
            if (!businessSchoolConfig.getPurchaseSwitch()) {
                right = "商学院报名临时关闭\n记得及时关注开放时间哦";
            } else if (riseMember != null && (RiseMember.HALF_ELITE == riseMember.getMemberTypeId() ||
                    RiseMember.ELITE == riseMember.getMemberTypeId() ||
                    RiseMember.HALF == riseMember.getMemberTypeId() ||
                    RiseMember.ANNUAL == riseMember.getMemberTypeId())) {
                left = 1;
            } else {
                // 查看是否开放报名
                if (ConfigUtils.getRisePayStopTime().before(new Date())) {
                    right = "谢谢您关注圈外商学院!\n本次报名已达到限额\n记得及时关注下期开放通知哦";
                } else {
                    left = 1;
                }
            }
        } else if (memberTypeId == RiseMember.CAMP) {
            MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();
            // 购买训练营
            if (riseMember != null && (RiseMember.HALF_ELITE == riseMember.getMemberTypeId() ||
                    RiseMember.ELITE == riseMember.getMemberTypeId())) {
                right = "您已经是圈外商学院学员，拥有主题训练营，无需重复报名\n如有疑问请在学习群咨询班长";
            } else {
                if (profile.getRiseMember() == Constants.RISE_MEMBER.MONTHLY_CAMP) {
                    List<RiseClassMember> classMembers = riseClassMemberDao.queryByProfileId(profileId);
                    List<Integer> months = classMembers.stream().map(RiseClassMember::getMonth).collect(Collectors.toList());
                    if (months.contains(monthlyCampConfig.getSellingMonth())) {
                        right = "您已经是" + monthlyCampConfig.getSellingMonth() + "月训练营用户";
                    } else {
                        left = 1;
                    }
                } else if (!monthlyCampConfig.getPurchaseSwitch()) {
                    right = "当月训练营已关闭报名";
                } else {
                    left = 1;
                }
            }
        } else if (memberTypeId == RiseMember.BS_APPLICATION) {
            left = 1;
            right = "正常";
        }
        return new MutablePair<>(left, right);
    }


    @Override
    public QuanwaiOrder signUpRiseMember(Integer profileId, Integer memberTypeId, List<Integer> couponId, Integer payType) {
        // 查询该openid是否是我们的用户
        Profile profile = accountService.getProfile(profileId);
        MemberType memberType = riseMemberTypeRepo.memberType(memberTypeId);
        Double fee;
        if (memberTypeId == RiseMember.ELITE) {
            // 报名商学院
            BusinessSchool bs = this.getSchoolInfoForPay(profileId);
            fee = bs.getFee();
        } else {
            fee = memberType.getFee();
        }
        Pair<String, Double> orderPair = generateOrderId(fee, couponId);

        Assert.notNull(profile, "用户信息错误");
        Assert.notNull(memberType, "会员类型错误");
        Assert.notNull(payType, "支付类型错误");
        QuanwaiOrder quanwaiOrder = this.createQuanwaiOrder(profile.getOpenid(),
                orderPair.getLeft(), fee, orderPair.getRight(), memberTypeId + "", memberType.getName(), QuanwaiOrder.FRAG_MEMBER,
                payType);

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
    public QuanwaiOrder signUpRiseMember(Integer profileId, Integer memberTypeId, List<Integer> couponId) {
        return this.signUpRiseMember(profileId, memberTypeId, couponId, QuanwaiOrder.PAY_WECHAT);
    }

    @Override
    public QuanwaiOrder signUpMonthlyCamp(Integer profileId, Integer memberTypeId, Integer couponId, Integer payType) {
        // 如果是购买训练营，配置 zk，查看当前月份
        Profile profile = accountService.getProfile(profileId);
        MemberType memberType = riseMemberTypeRepo.memberType(memberTypeId);
        Assert.notNull(profile, "用户不能为空");
        Assert.notNull(memberType, "会员类型错误");

        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();
        int sellingMonth = monthlyCampConfig.getSellingMonth();

        Double fee = memberType.getFee();
        Pair<String, Double> orderPair = generateOrderId(fee, couponId);

        QuanwaiOrder quanwaiOrder = createQuanwaiOrder(profile.getOpenid(), orderPair.getLeft(), fee, orderPair.getRight(),
                memberTypeId + "", sellingMonth + "月训练营", QuanwaiOrder.FRAG_CAMP, payType);

        // 插入训练营报名数据
        MonthlyCampOrder monthlyCampOrder = new MonthlyCampOrder();
        monthlyCampOrder.setOrderId(orderPair.getLeft());
        monthlyCampOrder.setOpenId(profile.getOpenid());
        monthlyCampOrder.setProfileId(profileId);
        monthlyCampOrder.setMonth(sellingMonth);
        monthlyCampOrderDao.insert(monthlyCampOrder);
        return quanwaiOrder;
    }

    @Override
    public QuanwaiOrder signUpMonthlyCamp(Integer profileId, Integer memberTypeId, Integer couponId) {
        return this.signUpMonthlyCamp(profileId, memberTypeId, couponId, QuanwaiOrder.PAY_WECHAT);
    }


    @Override
    public QuanwaiOrder signupBusinessSchoolApplication(Integer profileId, Integer memberTypeId, Integer couponId, Integer payType) {
        // 如果是购买训练营，配置 zk，查看当前月份
        Profile profile = accountService.getProfile(profileId);
        MemberType memberType = riseMemberTypeRepo.memberType(memberTypeId);
        Assert.notNull(profile, "用户不能为空");
        Assert.notNull(memberType, "会员类型错误");

        Double fee = memberType.getFee();
        Pair<String, Double> orderPair = generateOrderId(fee, couponId);

        QuanwaiOrder quanwaiOrder = createQuanwaiOrder(profile.getOpenid(),
                orderPair.getLeft(), fee, orderPair.getRight(),
                memberTypeId + "", "商学院申请", QuanwaiOrder.BS_APPLICATION, payType);

        // 插入训练营报名数据
        BusinessSchoolApplicationOrder bsOrder = new BusinessSchoolApplicationOrder();
        bsOrder.setOrderId(orderPair.getLeft());
        bsOrder.setOpenid(profile.getOpenid());
        bsOrder.setProfileId(profileId);
        businessSchoolApplicationOrderDao.insert(bsOrder);
        return quanwaiOrder;
    }

    @Override
    public QuanwaiOrder signupBusinessSchoolApplication(Integer profileId, Integer memberTypeId, Integer couponId) {
        return this.signupBusinessSchoolApplication(profileId, memberTypeId, couponId, QuanwaiOrder.PAY_WECHAT);
    }

    @Override
    public void payMonthlyCampSuccess(String orderId) {
        MonthlyCampOrder campOrder = monthlyCampOrderDao.loadCampOrder(orderId);
        Assert.notNull(campOrder, "训练营购买订单不能为空，orderId：" + orderId);

        Integer profileId = campOrder.getProfileId();

        // 更新 profile 表中状态
        Profile profile = accountService.getProfile(profileId);

        // RiseClassMember 新增记录
        insertMonthlyCampRiseClassMember(profileId);

        // 更新 RiseMember 表中信息
        updateMonthlyCampRiseMemberStatus(profile, orderId);

        // 送优惠券
        insertCampCoupon(profile);

        // 更新订单状态
        monthlyCampOrderDao.entry(orderId);

        // 发送 mq 消息，通知 platon 强行开启课程
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

        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();
        Integer year = monthlyCampConfig.getSellingYear();
        Integer month = monthlyCampConfig.getSellingMonth();

        sendPurchaseMessage(profile, RiseMember.CAMP, orderId, year, month);
        // 刷新相关状态
        refreshStatus(quanwaiOrderDao.loadOrder(orderId), orderId);
    }

    @Override
    public void unlockMonthlyCamp(Integer profileId) {
        Assert.notNull(profileId, "开课用户不能为空");

        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();

        Profile profile = accountService.getProfile(profileId);

        // RiseClassMember 新增记录
        insertMonthlyCampRiseClassMember(profileId);

        // 更新 RiseMember 表中信息
        updateMonthlyCampRiseMemberStatus(profile, null);

        // 赠送优惠券
        insertCampCoupon(profile);

        // 强开课程
        Integer monthlyCampSellingMonth = monthlyCampConfig.getSellingMonth();
        Integer category = accountService.loadUserScheduleCategory(profileId);
        List<CourseScheduleDefault> courseScheduleDefaults = courseScheduleDefaultDao.loadMajorCourseScheduleDefaultByCategory(category);
        List<Integer> problemIds = courseScheduleDefaults.stream()
                .filter(scheduleDefault -> monthlyCampSellingMonth.equals(scheduleDefault.getMonth()))
                .map(CourseScheduleDefault::getProblemId)
                .collect(Collectors.toList());

        problemIds.forEach(problemId -> {
            JSONObject json = new JSONObject();
            json.put("profileId", profileId);
            json.put("startDate", monthlyCampConfig.getOpenDate());
            json.put("closeDate", monthlyCampConfig.getCloseDate());
            json.put("problemId", problemId);
            try {
                openProblemPublisher.publish(json.toJSONString());
            } catch (ConnectException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        });

        // 刷新用户的会员状态
        try {
            freshLoginUserPublisher.publish(profile.getOpenid());
        } catch (ConnectException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * 新增训练营用户 RiseClassMember 记录
     */
    private void insertMonthlyCampRiseClassMember(Integer profileId) {
        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();

        int sellingYear = monthlyCampConfig.getSellingYear();
        int sellingMonth = monthlyCampConfig.getSellingMonth();

        String memberId = generateMemberId(sellingYear, sellingMonth, RiseClassMember.MONTHLY_CAMP);
        RiseClassMember riseClassMember = new RiseClassMember();
        riseClassMember.setClassName(generateClassName(memberId));
        riseClassMember.setMemberId(memberId);
        riseClassMember.setProfileId(profileId);
        riseClassMember.setYear(monthlyCampConfig.getSellingYear());
        riseClassMember.setMonth(monthlyCampConfig.getSellingMonth());
        riseClassMember.setActive(0);
        riseClassMemberDao.insert(riseClassMember);
    }

    /**
     * 新增商学院用户 RiseClassMember 记录
     */
    private void insertBusinessCollegeRiseClassMember(Integer profileId) {
        BusinessSchoolConfig businessSchoolConfig = cacheService.loadBusinessCollegeConfig();

        // 查看当月是否有训练营的其他记录，如果有则删除
        Integer sellingYear = businessSchoolConfig.getSellingYear();
        Integer sellingMonth = businessSchoolConfig.getSellingMonth();
        RiseClassMember riseClassMember = riseClassMemberDao.loadPurchaseRiseClassMember(profileId, sellingYear, sellingMonth);
        if (riseClassMember != null) {
            riseClassMemberDao.del(riseClassMember.getId());
        }

        // RiseClassMember 新增记录
        String memberId = generateMemberId(sellingYear, sellingMonth, RiseClassMember.BUSINESS_MEMBERSHIP);

        RiseClassMember classMember = new RiseClassMember();
        classMember.setClassName(generateClassName(memberId));
        classMember.setMemberId(memberId);
        classMember.setProfileId(profileId);
        classMember.setYear(businessSchoolConfig.getSellingYear());
        classMember.setMonth(businessSchoolConfig.getSellingMonth());
        classMember.setActive(0);
        riseClassMemberDao.insert(classMember);
    }

    /**
     * 购买完训练营之后，更新 RiseMember 表中的数据
     *
     * @param profile 用户 Profile
     */
    private void updateMonthlyCampRiseMemberStatus(Profile profile, String orderId) {
        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();

        // 每当在 RiseMember 表新增一种状态时候，预先在 RiseMember 表中其他数据置为过期
        RiseMember existRiseMember = this.currentRiseMember(profile.getId());
        if (existRiseMember == null) {
            // 添加会员表
            RiseMember riseMember = new RiseMember();
            riseMember.setOpenId(profile.getOpenid());
            if (orderId != null) {
                riseMember.setOrderId(orderId);
            } else {
                riseMember.setOrderId("manual");
            }
            riseMember.setProfileId(profile.getId());
            riseMember.setMemberTypeId(RiseMember.CAMP);
            riseMember.setOpenDate(monthlyCampConfig.getOpenDate());
            riseMember.setExpireDate(monthlyCampConfig.getCloseDate());
            riseMember.setExpired(false);
            riseMemberDao.insert(riseMember);
        } else {
            // 添加会员表
            RiseMember riseMember = new RiseMember();
            riseMember.setOpenId(profile.getOpenid());
            riseMember.setOrderId(orderId == null ? "manual" : orderId);
            riseMember.setProfileId(profile.getId());
            riseMember.setMemberTypeId(RiseMember.CAMP);
            riseMember.setOpenDate(monthlyCampConfig.getOpenDate());
            riseMember.setExpireDate(monthlyCampConfig.getCloseDate());

            if (existRiseMember.getMemberTypeId() == RiseMember.ANNUAL
                    || existRiseMember.getMemberTypeId() == RiseMember.HALF
                    || existRiseMember.getMemberTypeId() == RiseMember.HALF_ELITE
                    || existRiseMember.getMemberTypeId() == RiseMember.ELITE) {
                // 如果当前购买的人的身份是商学院会员或者专业版会员，则直接将新增的数据记录置为过期
                riseMember.setExpired(true);
                riseMember.setMemo("专业版购买训练营");
            } else {
                riseMemberDao.updateExpiredAhead(profile.getId());

                riseMember.setExpired(false);
                riseMemberDao.insert(riseMember);
            }
            riseMemberDao.insert(riseMember);
        }
    }

    /**
     * 放入训练营优惠券，金额 100，自购买起，两个月内过期
     *
     * @param profile 用户 Profile
     */
    private void insertCampCoupon(Profile profile) {
        // 送优惠券
        Coupon coupon = new Coupon();
        coupon.setOpenid(profile.getOpenid());
        coupon.setProfileId(profile.getId());
        coupon.setAmount(MONTHLY_CAMP_COUPON);
        coupon.setUsed(Coupon.UNUSED);
        coupon.setExpiredDate(DateUtils.afterMonths(new Date(), 2));
        coupon.setDescription("优惠券");
        couponDao.insert(coupon);
    }

    @Override
    public MonthlyCampOrder getMonthlyCampOrder(String orderId) {
        return monthlyCampOrderDao.loadCampOrder(orderId);
    }

    /**
     * 新学号格式：四位班级号（1701）+ 两位随着人数自增的值 + 一位身份信息（会员、课程、公益课、试听课） + 三位递增唯一序列（1701011001）
     */
    @Override
    public String generateMemberId(Integer year, Integer month, Integer identityType) {
        StringBuilder targetMemberId = new StringBuilder();

        String classPrefix = String.format("%02d", year % 2000) + String.format("%02d", month);

        String prefix = classPrefix + identityType;
        String key = "customer:memberId:" + prefix;

        redisUtil.lock("lock:memberId", (lock) -> {
            String sequence = redisUtil.get(key);
            if (sequence == null) {
                sequence = "001";
            } else {
                sequence = String.format("%03d", Integer.parseInt(sequence) + 1);
            }
            redisUtil.set(key, sequence, TimeUnit.DAYS.toSeconds(60));
            int sequenceInt = Integer.parseInt(sequence);
            targetMemberId.append(classPrefix);
            if (RiseClassMember.BUSINESS_MEMBERSHIP == identityType) {
                targetMemberId.append(String.format("%02d", (sequenceInt % 200 == 0 ? sequenceInt / 200 : sequenceInt / 200 + 1) * 2 - 1));
            } else if (RiseClassMember.MONTHLY_CAMP == identityType) {
                targetMemberId.append(String.format("%02d", (sequenceInt % 200 == 0 ? sequenceInt / 200 : sequenceInt / 200 + 1) * 2));
            }
            targetMemberId.append(identityType);
            targetMemberId.append(String.format("%03d", sequenceInt % 200 == 0 ? 1 : sequenceInt % 200));
        });

        return targetMemberId.toString();
    }

    private String generateClassName(String memberId) {
        return memberId.substring(0, 6);
    }

    @Override
    public void payRiseSuccess(String orderId) {
        RiseOrder riseOrder = riseOrderDao.loadOrder(orderId);

        BusinessSchoolConfig businessSchoolConfig = cacheService.loadBusinessCollegeConfig();

        try {
            RiseMember exist = riseMemberDao.loadByOrderId(orderId);
            if (riseOrder.getEntry() && exist != null && !exist.getExpired() && DateUtils.isSameDate(exist.getAddTime(), new Date())) {
                // 这个单子已经成功，且已经插入了 riseMember，并且未过期，并且是今天的
                messageService.sendAlarm("报名模块次级异常", "微信多次回调",
                        "中", "订单id:" + orderId, "再次处理今天已经插入的 risemember");
                return;
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        riseOrderDao.entry(orderId);
        String openId = riseOrder.getOpenid();
        MemberType memberType = riseMemberTypeRepo.memberType(riseOrder.getMemberType());
        if (RiseMember.ELITE == memberType.getId()) {
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

            if (existRiseMember != null && existRiseMember.getMemberTypeId().equals(RiseMember.ELITE)) {
                riseMember.setExpireDate(DateUtils.afterMonths(existRiseMember.getExpireDate(), 12));
                // 续费，继承OpenDate
                riseMember.setOpenDate(existRiseMember.getOpenDate());
            } else if (existRiseMember != null && existRiseMember.getMemberTypeId().equals(RiseMember.HALF_ELITE)) {
                // TODO 特殊处理 查看当前身份，商学院会员升级方式保留，但是精英版半年只增加半年时间
                riseMember.setExpireDate(DateUtils.afterMonths(existRiseMember.getExpireDate(), 6));
                // 续费，继承OpenDate
                riseMember.setOpenDate(existRiseMember.getOpenDate());
            } else {
                // 非续费，查询本次开营时间
                riseMember.setOpenDate(businessSchoolConfig.getOpenDate());
                riseMember.setExpireDate(DateUtils.afterMonths(businessSchoolConfig.getOpenDate(), 12));

                // 精英会员一年
                // RiseClassMember 新增会员记录
                insertBusinessCollegeRiseClassMember(riseOrder.getProfileId());
                profileDao.initOnceRequestCommentCount(openId);
            }
            riseMember.setExpired(false);
            riseMemberDao.insert(riseMember);

            // 所有计划设置为会员
            List<ImprovementPlan> plans = improvementPlanDao.loadUserPlans(riseOrder.getOpenid());
            // 不是会员的计划，设置一下
            // 给精英版正在进行的 plan + 1 个求点评次数
            // 非精英版或者不是正在进行的，不加点评次数
            plans.stream().filter(plan -> !plan.getRiseMember()).forEach(plan -> {
                // 不是会员的计划，设置一下
                plan.setCloseDate(DateUtils.afterDays(new Date(), PROBLEM_MAX_LENGTH));
                if (plan.getStatus().equals(1) && (memberType.getId().equals(3) || memberType.getId().equals(4))) {
                    // 给精英版正在进行的 plan + 1 个求点评次数
                    improvementPlanDao.becomeRiseEliteMember(plan);
                } else {
                    // 非精英版或者不是正在进行的，不加点评次数
                    improvementPlanDao.becomeRiseMember(plan);
                }
            });
            Profile profile = accountService.getProfile(openId, false);
            // 发送模板消息
            sendPurchaseMessage(profile, memberType.getId(), orderId, businessSchoolConfig.getSellingYear(), businessSchoolConfig.getSellingMonth());
        } else {
            logger.error("该会员ID异常{}", memberType);
            messageService.sendAlarm("报名模块出错", "会员id异常", "高", "订单id:" + orderId, "会员类型异常");
        }
    }

    private void sendPurchaseMessage(Profile profile, Integer memberTypeId, String orderId, Integer year, Integer month) {
        Assert.notNull(profile, "openid不能为空");
        logger.info("发送欢迎消息给付费用户{}", profile.getOpenid());
        boolean isFull = profile.getIsFull() == 1;
        boolean isBindMobile = profile.getMobileNo() != null;
        String detailUrl = ConfigUtils.domainName() + "/rise/static/customer/profile?goRise=true";
        String mobileUrl = ConfigUtils.domainName() + "/rise/static/customer/mobile/check?goRise=true";
        String sendUrl = isFull ? isBindMobile ? null : mobileUrl : detailUrl;

        List<OperateRotate> operateRotates = operateRotateDao.loadAllOperateRotates();

        switch (memberTypeId) {
            case RiseMember.ELITE: {
                List<OperateRotate> riseMemberOperateRotates = operateRotates.stream()
                        .filter(operateRotate -> RISEMEMBER_OPERATEROTATE_SCENE_CODE.equals(operateRotate.getSceneCode()))
                        .sorted(Comparator.comparingInt(OperateRotate::getSequence))
                        .collect(Collectors.toList());

                redisUtil.lock("operateRotate:riseMember:paySuccess", lock -> {
                    String riseMemberKey = "operateRotate:" + RISEMEMBER_OPERATEROTATE_SCENE_CODE + ":index";
                    String riseMemberIndexStr = redisUtil.get(riseMemberKey);
                    int riseMemberIndex = riseMemberIndexStr == null ? 1 : Integer.parseInt(riseMemberIndexStr);
                    redisUtil.set(riseMemberKey, riseMemberIndex + 1);
                    logger.info("riseMemberIndex: {}", riseMemberIndex);
                    int sequence = riseMemberIndex % OPERATEROTATE_SWITCH_SIZE == 0 ? riseMemberIndex / OPERATEROTATE_SWITCH_SIZE : riseMemberIndex / OPERATEROTATE_SWITCH_SIZE + 1;
                    logger.info("sequence: {}", sequence);
                    OperateRotate operateRotate = riseMemberOperateRotates.get(sequence % riseMemberOperateRotates.size() == 0 ? riseMemberOperateRotates.size() - 1 : sequence % riseMemberOperateRotates.size() - 1);
                    Assert.notNull(operateRotate);
                    logger.info("operateRotate mediaId: {}", operateRotate.getMediaId());
                    RiseClassMember riseClassMember = riseClassMemberDao.loadPurchaseRiseClassMember(profile.getId(), year, month);
                    String entryCode = riseClassMember.getMemberId();
                    logger.info("发送会员数据");
                    // 发送消息给一年精英版的用户
                    customerMessageService.sendCustomerMessage(profile.getOpenid(), operateRotate.getMediaId(), Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        logger.error(e.getLocalizedMessage(), e);
                    }
                    customerMessageService.sendCustomerMessage(profile.getOpenid(), entryCode, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                    if (sendUrl != null) {
                        messageService.sendMessage("点此完善个人信息，才能参加校友会，获取更多人脉资源喔！", Objects.toString(profile.getId()), MessageService.SYSTEM_MESSAGE, sendUrl);
                    }
                });
                break;
            }
            case RiseMember.CAMP: {
                List<OperateRotate> monthlyCampOperateRotates = operateRotates.stream()
                        .filter(operateRotate -> MONTHLYCAMP_OPERATEROTATE_SCENE_CODE.equals(operateRotate.getSceneCode()))
                        .sorted(Comparator.comparingInt(OperateRotate::getSequence))
                        .collect(Collectors.toList());

                redisUtil.lock("operateRotate:monthlyCamp:paySuccess", lock -> {
                    String monthlyCampKey = "operateRotate:" + MONTHLYCAMP_OPERATEROTATE_SCENE_CODE + ":index";
                    String monthlyCampIndexStr = redisUtil.get(monthlyCampKey);
                    int monthlyCampIndex = monthlyCampIndexStr == null ? 1 : Integer.parseInt(monthlyCampIndexStr);
                    redisUtil.set(monthlyCampKey, monthlyCampIndex + 1);
                    int sequence = monthlyCampIndex % OPERATEROTATE_SWITCH_SIZE == 0 ? monthlyCampIndex / OPERATEROTATE_SWITCH_SIZE : monthlyCampIndex / OPERATEROTATE_SWITCH_SIZE + 1;
                    OperateRotate operateRotate = monthlyCampOperateRotates.get(sequence % monthlyCampOperateRotates.size() == 0 ? monthlyCampOperateRotates.size() - 1 : sequence % monthlyCampOperateRotates.size() - 1);
                    Assert.notNull(operateRotate);

                    RiseClassMember riseClassMember = riseClassMemberDao.loadPurchaseRiseClassMember(profile.getId(), year, month);
                    String entryCode = riseClassMember.getMemberId();

                    logger.info("发送训练营数据");
                    // 发送消息给训练营购买用户
                    customerMessageService.sendCustomerMessage(profile.getOpenid(), operateRotate.getMediaId(), Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        logger.error(e.getLocalizedMessage(), e);
                    }
                    customerMessageService.sendCustomerMessage(profile.getOpenid(), entryCode, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                    if (sendUrl != null) {
                        messageService.sendMessage("点此完善个人信息，才能参加校友会，获取更多人脉资源喔！", Objects.toString(profile.getId()), MessageService.SYSTEM_MESSAGE, sendUrl);
                    }
                });
                break;
            }
            default: {
                messageService.sendAlarm("报名模块出错", "报名后发送消息", "中",
                        "订单id:" + orderId + "\nprofileId:" + profile.getId(), "会员类型异常");
            }
        }
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
    public List<MemberType> getMemberTypesPayInfo() {
        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();

        List<MemberType> memberTypes = riseMemberTypeRepo.memberTypes();
        // 写入会员开始和结束时间
        memberTypes.forEach(item -> {
            item.setStartTime(DateUtils.parseDateToStringByCommon(new Date()));
            if (item.getId().equals(RiseMember.CAMP)) {
                item.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(monthlyCampConfig.getCloseDate(), 1)));
            } else {
                item.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(DateUtils.afterMonths(new Date(), item.getOpenMonth()), 1)));
            }
        });
        return memberTypes;
    }

    @Override
    public List<MemberType> getMemberTypesPayInfo(Integer profileId) {
        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();
        BusinessSchoolConfig businessSchoolConfig = cacheService.loadBusinessCollegeConfig();
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);

        List<MemberType> memberTypes = riseMemberTypeRepo.memberTypes();
        // 写入会员开始和结束时间
        for (MemberType memberType : memberTypes) {
            if (memberType.getId().equals(RiseMember.CAMP)) {
                // 训练营类型
                memberType.setStartTime(DateUtils.parseDateToStringByCommon(monthlyCampConfig.getOpenDate()));
                memberType.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(monthlyCampConfig.getCloseDate(), 1)));
            } else if (memberType.getId().equals(RiseMember.ELITE) || memberType.getId().equals(RiseMember.HALF_ELITE)) {
                // 商学院类型（一年、半年）
                if (riseMember != null && (riseMember.getMemberTypeId().equals(RiseMember.ELITE) || riseMember.getMemberTypeId().equals(RiseMember.HALF_ELITE))) {
                    // 商学院会员续费
                    memberType.setStartTime(DateUtils.parseDateToStringByCommon(riseMember.getExpireDate()));
                    // TODO 精英版半年升级商学院
                    if (riseMember.getMemberTypeId().equals(RiseMember.HALF_ELITE)) {
                        memberType.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(DateUtils.afterMonths(riseMember.getExpireDate(), 6), 1)));
                    } else {
                        memberType.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(DateUtils.afterMonths(riseMember.getExpireDate(), memberType.getOpenMonth()), 1)));
                    }
                } else {
                    // 商学院报名
                    memberType.setStartTime(DateUtils.parseDateToStringByCommon(businessSchoolConfig.getOpenDate()));
                    memberType.setEndTime(DateUtils.parseDateToStringByCommon(
                            DateUtils.beforeDays(DateUtils.afterMonths(businessSchoolConfig.getOpenDate(), memberType.getOpenMonth()), 1)));
                }
            } else {
                memberType.setStartTime(DateUtils.parseDateToStringByCommon(new Date()));
                memberType.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(DateUtils.afterMonths(new Date(), memberType.getOpenMonth()), 1)));
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
            // 报名商学院
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
    public BusinessSchool getSchoolInfoForPay(Integer profileId) {
        BusinessSchool businessSchool = new BusinessSchool();
        Double fee;
        RiseMember riseMember = this.currentRiseMember(profileId);
        MemberType memberType = this.getMemberType(RiseMember.ELITE);
        businessSchool.setIsBusinessStudent(false);
        if (riseMember != null) {
            switch (riseMember.getMemberTypeId()) {
                case RiseMember.ELITE:
                    fee = memberType.getFee();
                    businessSchool.setIsBusinessStudent(true);
                    break;
                case RiseMember.HALF_ELITE:
                    // TODO 对于精英版半年版的学员，金额更改为 1800
                    fee = 1800.0;
                    businessSchool.setIsBusinessStudent(true);
                    break;
                case RiseMember.ANNUAL:
                case RiseMember.HALF:
                    fee = normalMemberDiscount(riseMember, memberType.getFee());
                    if (!ConfigUtils.reducePriceForNotElite()) {
                        // 关闭减价，恢复原价
                        fee = memberType.getFee();
                    }
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

        businessSchool.setFee(fee);
        return businessSchool;
    }

    @Override
    public RiseMember getCurrentRiseMemberStatus(Integer profileId) {
        BusinessSchoolConfig businessSchoolConfig = cacheService.loadBusinessCollegeConfig();

        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        if (riseMember.getMemberTypeId().equals(RiseMember.ELITE)) {
            riseMember.setStartTime(DateUtils.parseDateToStringByCommon(DateUtils.afterMonths(riseMember.getExpireDate(), -12)));
        } else {
            riseMember.setStartTime(DateUtils.parseDateToStringByCommon(new Date()));
        }
        riseMember.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(riseMember.getExpireDate(), 1)));

        Integer year = businessSchoolConfig.getSellingYear();
        Integer month = businessSchoolConfig.getSellingMonth();

        RiseClassMember riseClassMember = riseClassMemberDao.loadPurchaseRiseClassMember(profileId, year, month);
        if (riseClassMember != null) {
            riseMember.setEntryCode(riseClassMember.getMemberId());
        }
        return riseMember;
    }

    @Override
    public RiseMember getCurrentMonthlyCampStatus(Integer profileId) {
        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();

        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        riseMember.setStartTime(DateUtils.parseDateToStringByCommon(new Date()));
        riseMember.setEndTime(DateUtils.parseDateToStringByCommon(monthlyCampConfig.getCloseDate()));

        Integer year = monthlyCampConfig.getSellingYear();
        Integer month = monthlyCampConfig.getSellingMonth();

        RiseClassMember riseClassMember = riseClassMemberDao.loadPurchaseRiseClassMember(profileId, year, month);
        if (riseClassMember != null) {
            riseMember.setEntryCode(riseClassMember.getMemberId());
        }
        return riseMember;
    }

    @Override
    public List<RiseMember> loadPersonalAllRiseMembers(Integer profileId) {
        return riseMemberDao.loadPersonalAll(profileId);
    }

    @Override
    public void payApplicationSuccess(String orderId) {
        BusinessSchoolApplicationOrder order = businessSchoolApplicationOrderDao.loadBusinessSchoolApplicationOrder(orderId);
        Assert.notNull(order, "商学院申请购买订单不能为空，orderId：" + orderId);
        BusinessSchoolApplication apply = businessSchoolApplicationDao.loadLatestInvalidApply(order.getProfileId());
        if (apply == null) {
            // 更新订单状态
            businessSchoolApplicationOrderDao.paid(orderId);
        } else {
            // 更新最后一次无效申请
            businessSchoolApplicationDao.validApply(orderId, apply.getId());
            businessSchoolApplicationOrderDao.paid(orderId);
        }

    }

    @Override
    public BusinessSchoolApplicationOrder getBusinessSchoolOrder(String orderId) {
        return businessSchoolApplicationOrderDao.loadBusinessSchoolApplicationOrder(orderId);
    }

    @Override
    public boolean isAppliedBefore(Integer profileId) {
        BusinessSchoolApplicationOrder businessSchoolApplicationOrder = businessSchoolApplicationOrderDao.loadBusinessSchoolApplicationNoAppliedOrder(profileId);
        return businessSchoolApplicationOrder != null;
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
            messageService.sendAlarm("报名模块出错", "发送支付成功mq失败", "高", "订单id:" + orderId, e.getLocalizedMessage());
        }
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
            Coupon coupon = costRepo.getCoupon(couponId);
            Assert.notNull(coupon, "优惠券无效");
            discount = costRepo.discount(fee, orderId, coupon);
        }
        return new MutablePair<>(orderId, discount);
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
            List<Coupon> coupons = couponIdGroup.stream().map(costRepo::getCoupon).collect(Collectors.toList());
            Assert.notEmpty(coupons, "优惠券无效");
            discount = costRepo.discount(fee, orderId, coupons);
        }
        return new MutablePair<>(orderId, discount);
    }

    private QuanwaiOrder createQuanwaiOrder(String openId, String orderId, Double fee, Double discount, String goodsId, String goodsName, String goodsType, Integer payType) {
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
        quanwaiOrder.setPayType(payType);
        quanwaiOrderDao.insert(quanwaiOrder);
        return quanwaiOrder;
    }

    /**
     * 默认微信方式支付
     */
    private QuanwaiOrder createQuanwaiOrder(String openId, String orderId, Double fee, Double discount, String goodsId, String goodsName, String goodsType) {
        return this.createQuanwaiOrder(openId, orderId, fee, discount, goodsId, goodsName, goodsType, QuanwaiOrder.PAY_WECHAT);
    }

    // 专业版折价方案
    private Double normalMemberDiscount(RiseMember riseMember, Double price) {
        if (riseMember != null) {
            if (riseMember.getMemberTypeId() == RiseMember.ANNUAL) {
                // 半年版升级价格公式 = 商学院价格 - 一年版剩余天数/365*一年版原价
                Date expireDate = riseMember.getExpireDate();
                int remain = DateUtils.interval(expireDate);
                price = CommonUtils.substract(price, remain / 365.0 * 880);
            } else if (riseMember.getMemberTypeId() == RiseMember.HALF) {
                // 半年版升级价格公式 = 商学院价格 - 半年版剩余天数/182.5*半年版原价
                Date expireDate = riseMember.getExpireDate();
                int remain = DateUtils.interval(expireDate);
                price = CommonUtils.substract(price, remain / 182.5 * 580);
            }
        }
        //取整
        price = price.intValue() + 0.0d;

        return price;
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
}
