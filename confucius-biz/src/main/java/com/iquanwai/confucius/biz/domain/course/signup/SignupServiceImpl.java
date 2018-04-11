package com.iquanwai.confucius.biz.domain.course.signup;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.RedisUtil;
import com.iquanwai.confucius.biz.dao.apply.BusinessSchoolApplicationDao;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.fragmentation.*;
import com.iquanwai.confucius.biz.dao.wx.QuanwaiOrderDao;
import com.iquanwai.confucius.biz.domain.fragmentation.CacheService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.domain.message.ShortMessage;
import com.iquanwai.confucius.biz.domain.message.ShortMessageService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.customer.CustomerMessageService;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.OperateRotate;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.*;
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
import java.util.*;
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
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private ShortMessageService shortMessageService;
    @Autowired
    private RiseMemberManager riseMemberManager;

    private final static int PROBLEM_MAX_LENGTH = 30; //课程最长开放时间

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, SoftReference<QuanwaiClass>> classMap = Maps.newHashMap();
    private Map<Integer, CourseIntroduction> courseMap = Maps.newHashMap();

    private RabbitMQPublisher rabbitMQPublisher;
    private RabbitMQPublisher paySuccessPublisher;
    private RabbitMQPublisher freshLoginUserPublisher;

    private static final String RISEMEMBER_OPERATEROTATE_SCENE_CODE = "rise_member_pay_success";
    private static final String MONTHLYCAMP_OPERATEROTATE_SCENE_CODE = "monthly_camp_pay_success";
    private static final int OPERATEROTATE_SWITCH_SIZE = 200;
    private final static int BUSINESS_SCHOOL_LEARNING_MONTHS = 12;
    private final static int BUSINESS_THOUGHT_LEARNING_MONTHS = 6;


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
    public Pair<Boolean, String> risePurchaseCheck(Integer profileId, Integer memberTypeId) {
        Pair<Boolean, String> pass = Pair.of(false, "类型异常");
        // common login
//        boolean exists = riseMemberManager.member(profileId).stream().anyMatch(item -> item.getMemberTypeId().equals(memberTypeId));
//        if (exists) {
//            return Pair.of(false, "您已经有该权限，无需重复报名");
//        }

        if (memberTypeId == RiseMember.ELITE) {
            pass = accountService.hasPrivilegeForBusinessSchool(profileId);
        } else if (memberTypeId == RiseMember.BS_APPLICATION) {
            pass = accountService.hasPrivilegeForApply(profileId, Constants.Project.CORE_PROJECT);
        } else if (memberTypeId == RiseMember.BUSINESS_THOUGHT) {
            pass = accountService.hasPrivilegeForMiniMBA(profileId);
        } else if (memberTypeId == RiseMember.CAMP) {
            pass = accountService.hasPrivilegeForCamp(profileId);
        } else if (memberTypeId == RiseMember.BUSINESS_THOUGHT_APPLY) {
            pass = accountService.hasPrivilegeForApply(profileId, Constants.Project.BUSINESS_THOUGHT_PROJECT);
        }
        return pass;
    }


    @Override
    public QuanwaiOrder signUpRiseMember(Integer profileId, Integer memberTypeId, List<Integer> couponId, Integer payType) {
        // 查询该openid是否是我们的用户
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
        MemberType memberType = riseMemberTypeRepo.memberType(memberTypeId);
        Assert.notNull(memberType, "会员类型错误");

        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();
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
        MemberType memberType = riseMemberTypeRepo.memberType(memberTypeId);
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
    public void payMonthlyCampSuccess(String orderId) {
        MonthlyCampOrder campOrder = monthlyCampOrderDao.loadCampOrder(orderId);
        Assert.notNull(campOrder, "专项课购买订单不能为空，orderId：" + orderId);

        Integer profileId = campOrder.getProfileId();

        // 更新 profile 表中状态
        Profile profile = accountService.getProfile(profileId);

        // RiseClassMember 新增记录
        insertMonthlyCampRiseClassMember(profileId);

        // 更新 RiseMember 表中信息
        updateMonthlyCampRiseMemberStatus(profile, orderId);

        // 送优惠券
//        insertCampCoupon(profile);

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
        refreshStatus(quanwaiOrderDao.loadOrder(orderId), profile, orderId);
    }

    /**
     * 新增专项课用户 RiseClassMember 记录
     */
    private void insertMonthlyCampRiseClassMember(Integer profileId) {
        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();

        int sellingYear = monthlyCampConfig.getSellingYear();
        int sellingMonth = monthlyCampConfig.getSellingMonth();

        String memberId = generateMemberId(sellingYear, sellingMonth, RiseClassMember.MONTHLY_CAMP);
        RiseClassMember riseClassMember = new RiseClassMember();
        riseClassMember.setClassName(generateClassName(memberId));
//        riseClassMember.setMemberId(memberId);
        riseClassMember.setProfileId(profileId);
        riseClassMember.setYear(monthlyCampConfig.getSellingYear());
        riseClassMember.setMonth(monthlyCampConfig.getSellingMonth());
        riseClassMember.setActive(0);
        riseClassMemberDao.insert(riseClassMember);

        accountService.updateMemberId(profileId, memberId);
    }

    /**
     * 新增商学院用户 RiseClassMember 记录
     */
    private void insertBusinessCollegeRiseClassMember(Integer profileId) {
        BusinessSchoolConfig businessSchoolConfig = cacheService.loadBusinessCollegeConfig(RiseMember.ELITE);

        // 查看当月是否有专项课的其他记录，如果有则删除
        Integer sellingYear = businessSchoolConfig.getSellingYear();
        Integer sellingMonth = businessSchoolConfig.getSellingMonth();
        RiseClassMember riseClassMember = riseClassMemberDao.queryByProfileIdAndTime(profileId, sellingYear, sellingMonth);
        if (riseClassMember != null) {
            riseClassMemberDao.del(riseClassMember.getId());
        }

        // RiseClassMember 新增记录
        String memberId = generateMemberId(sellingYear, sellingMonth, RiseClassMember.BUSINESS_MEMBERSHIP);

        RiseClassMember classMember = new RiseClassMember();
        classMember.setClassName(generateClassName(memberId));
//        classMember.setMemberId(memberId);
        classMember.setProfileId(profileId);
        classMember.setYear(businessSchoolConfig.getSellingYear());
        classMember.setMonth(businessSchoolConfig.getSellingMonth());
        classMember.setActive(0);
        riseClassMemberDao.insert(classMember);
        Profile profile = accountService.getProfile(profileId);
        accountService.updateMemberId(profileId, memberId);
    }

    /**
     * 购买完专项课之后，更新 RiseMember 表中的数据
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
            riseMember.setVip(false);
            riseMemberDao.insert(riseMember);
        } else {
            // 添加会员表
            RiseMember riseMember = new RiseMember();
            riseMember.setOrderId(orderId == null ? "manual" : orderId);
            riseMember.setProfileId(profile.getId());
            riseMember.setMemberTypeId(RiseMember.CAMP);
            riseMember.setOpenDate(monthlyCampConfig.getOpenDate());
            riseMember.setExpireDate(monthlyCampConfig.getCloseDate());
            riseMember.setVip(false);

            if (existRiseMember.getMemberTypeId() == RiseMember.ANNUAL
                    || existRiseMember.getMemberTypeId() == RiseMember.HALF
                    || existRiseMember.getMemberTypeId() == RiseMember.HALF_ELITE
                    || existRiseMember.getMemberTypeId() == RiseMember.ELITE) {
                // 如果当前购买的人的身份是商学院会员或者专业版会员，则直接将新增的数据记录置为过期
                riseMember.setExpired(true);
                riseMember.setMemo("专业版购买专项课");
            } else {
                riseMemberDao.updateExpiredAhead(profile.getId());

                riseMember.setExpired(false);
            }
            riseMemberDao.insert(riseMember);
        }
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
        // 支付成功
        riseOrderDao.entry(orderId);
        // 会员项目信息
        MemberType memberType = riseMemberTypeRepo.memberType(riseOrder.getMemberType());
        // 开课时间
        BusinessSchoolConfig businessSchoolConfig = cacheService.loadBusinessCollegeConfig(memberType.getId());
        // 查看是否存在现成会员数据
        RiseMember existRiseMember = riseMemberDao.loadValidRiseMemberByMemberTypeId(riseOrder.getProfileId(), Lists.newArrayList(riseOrder.getMemberType())).stream().findFirst().orElse(null);
        // 添加会员表
        RiseMember riseMember = new RiseMember();
        riseMember.setOrderId(riseOrder.getOrderId());
        riseMember.setProfileId(riseOrder.getProfileId());
        riseMember.setMemberTypeId(memberType.getId());
        riseMember.setExpired(false);
        riseMember.setVip(false);
        // 学习时间
        Integer learningMonthDate = 12;
        if (RiseMember.ELITE == memberType.getId()) {
            learningMonthDate = BUSINESS_SCHOOL_LEARNING_MONTHS;
        } else if (RiseMember.BUSINESS_THOUGHT == memberType.getId()) {
            learningMonthDate = BUSINESS_THOUGHT_LEARNING_MONTHS;
        } else {
            logger.error("该会员ID异常{}", memberType);
            messageService.sendAlarm("报名模块出错", "会员id异常", "高", "订单id:" + orderId, "会员类型异常");
        }

        // 所有计划设置为会员
        List<ImprovementPlan> plans = improvementPlanDao.loadAllPlans(riseOrder.getProfileId());
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

        if (existRiseMember == null) {
            // 非续费，查询本次开营时间
            riseMember.setOpenDate(businessSchoolConfig.getOpenDate());
            riseMember.setExpireDate(DateUtils.afterMonths(businessSchoolConfig.getOpenDate(), learningMonthDate));
            insertBusinessCollegeRiseClassMember(riseOrder.getProfileId());
            profileDao.initOnceRequestCommentCount(riseOrder.getProfileId());
        } else {
            // 如果存在，则将已经存在的 riseMember 数据置为已过期
            riseMemberDao.expired(existRiseMember.getId());
            riseMember.setExpireDate(DateUtils.afterMonths(existRiseMember.getExpireDate(), learningMonthDate));
            // 续费，继承OpenDate
            riseMember.setOpenDate(existRiseMember.getOpenDate());
        }
        riseMemberDao.insert(riseMember);

        Profile profile = accountService.getProfile(riseOrder.getProfileId());
        // 发送模板消息
        sendPurchaseMessage(profile, memberType.getId(), orderId, businessSchoolConfig.getSellingYear(), businessSchoolConfig.getSellingMonth());
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

                    String entryCode = profile.getMemberId();
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
                    // 发短信提醒
                    if (profile.getMobileNo() != null) {
                        ShortMessage shortMessage = new ShortMessage();
                        shortMessage.setProfileId(profile.getId());
                        shortMessage.setContent(profile.getNickname() + " 你好，欢迎加入圈外商学院。请添加你的班主任微信：MBAsalmon，接下来班主任将帮助你更好地学习。" +
                                "快回复你的学号（学号：" + entryCode + "）向班主任报道吧！");
                        shortMessage.setNickname(shortMessage.getNickname());
                        shortMessage.setType(ShortMessage.BUSINESS);
                        shortMessage.setPhone(profile.getMobileNo());
                        shortMessageService.sendMessage(shortMessage);
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

                    String entryCode = profile.getMemberId();

                    logger.info("发送专项课数据");
                    // 发送消息给专项课购买用户
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
            case RiseMember.BUSINESS_THOUGHT: {
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

                    String entryCode = profile.getMemberId();
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
                    // 发短信提醒
                    if (profile.getMobileNo() != null) {
                        ShortMessage shortMessage = new ShortMessage();
                        shortMessage.setProfileId(profile.getId());
                        shortMessage.setContent(profile.getNickname() + " 你好，欢迎加入商业进阶课。请添加你的班主任微信：MBAsalmon，接下来班主任将帮助你更好地学习。" +
                                "快回复你的学号（学号：" + entryCode + "）向班主任报道吧！");
                        shortMessage.setNickname(shortMessage.getNickname());
                        shortMessage.setType(ShortMessage.BUSINESS);
                        shortMessage.setPhone(profile.getMobileNo());
                        shortMessageService.sendMessage(shortMessage);
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
        BusinessSchoolConfig businessSchoolConfig = cacheService.loadBusinessCollegeConfig(RiseMember.ELITE);
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);

        List<MemberType> memberTypes = riseMemberTypeRepo.memberTypes();
        // 写入会员开始和结束时间
        for (MemberType memberType : memberTypes) {
            if (memberType.getId().equals(RiseMember.CAMP)) {
                // 专项课类型
                memberType.setStartTime(DateUtils.parseDateToStringByCommon(monthlyCampConfig.getOpenDate()));
                memberType.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(monthlyCampConfig.getCloseDate(), 1)));
            } else if (memberType.getId().equals(RiseMember.ELITE) || memberType.getId().equals(RiseMember.HALF_ELITE)) {
                // 商学院类型（一年、半年）
                if (riseMember != null && (riseMember.getMemberTypeId().equals(RiseMember.ELITE) || riseMember.getMemberTypeId().equals(RiseMember.HALF_ELITE))) {
                    // 商学院会员续费
                    memberType.setStartTime(DateUtils.parseDateToStringByCommon(riseMember.getExpireDate()));
                    memberType.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(
                            DateUtils.afterMonths(riseMember.getExpireDate(), memberType.getOpenMonth()), 1)));
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
    public List<RiseMember> currentRiseMembers(Integer profileId) {
        List<RiseMember> riseMembers = riseMemberDao.loadValidRiseMembers(profileId);
        riseMembers.forEach(riseMember -> {
            riseMember.setStartTime(DateUtils.parseDateToStringByCommon(riseMember.getAddTime()));
            riseMember.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(riseMember.getExpireDate(), 1)));
        });
        return riseMembers;
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
        Profile profile = accountService.getProfile(profileId);
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        if (riseMember.getMemberTypeId().equals(RiseMember.ELITE)) {
            riseMember.setStartTime(DateUtils.parseDateToStringByCommon(DateUtils.afterMonths(riseMember.getExpireDate(), -12)));
        } else {
            riseMember.setStartTime(DateUtils.parseDateToStringByCommon(new Date()));
        }
        riseMember.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(riseMember.getExpireDate(), 1)));

        if (profile != null) {
            riseMember.setEntryCode(profile.getMemberId());
        }
        return riseMember;
    }

    @Override
    public RiseMember getCurrentMonthlyCampStatus(Integer profileId) {
        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();

        Profile profile = accountService.getProfile(profileId);
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        riseMember.setStartTime(DateUtils.parseDateToStringByCommon(new Date()));
        riseMember.setEndTime(DateUtils.parseDateToStringByCommon(monthlyCampConfig.getCloseDate()));

        if (profile != null) {
            riseMember.setEntryCode(profile.getMemberId());
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

        QuanwaiOrder quanwaiOrder = quanwaiOrderDao.loadOrder(orderId);
        BusinessSchoolApplication apply = null;

        if (quanwaiOrder.getGoodsId().equals(Integer.valueOf(RiseMember.BS_APPLICATION).toString())) {
            apply = businessSchoolApplicationDao.loadLatestInvalidApply(order.getProfileId(), Constants.Project.CORE_PROJECT);
        } else if (quanwaiOrder.getGoodsId().equals(Integer.valueOf(RiseMember.BUSINESS_THOUGHT_APPLY).toString())) {
            apply = businessSchoolApplicationDao.loadLatestInvalidApply(order.getProfileId(), Constants.Project.BUSINESS_THOUGHT_PROJECT);
        }

        if (apply == null) {
            // 更新订单状态
            businessSchoolApplicationOrderDao.paid(orderId);
        } else {
            // 更新最后一次无效申请
            businessSchoolApplicationDao.validApply(orderId, apply.getId());
            businessSchoolApplicationOrderDao.paid(orderId);

            // 提交有效申请
            operationLogService.trace(order.getProfileId(), "submitValidApply");
        }
    }

    @Override
    public BusinessSchoolApplicationOrder getBusinessSchoolOrder(String orderId) {
        return businessSchoolApplicationOrderDao.loadBusinessSchoolApplicationOrder(orderId);
    }

    private void refreshStatus(QuanwaiOrder quanwaiOrder, Profile profile, String orderId) {
        // 刷新会员状态
        try {
            freshLoginUserPublisher.publish(profile.getUnionid());
        } catch (ConnectException e) {
            logger.error("发送会员信息更新mq失败", e);
        }
        // 更新优惠券使用状态
        if (quanwaiOrder.getDiscount() != 0.0) {
            logger.info("{}使用优惠券", profile.getOpenid());
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
    public String getSubscribeQrCodeForApply(Integer memberTypeId) {
        String qrCodeUrl = "";
        switch (memberTypeId) {
            case RiseMember.BS_APPLICATION:
                qrCodeUrl = ConfigUtils.getCoreApplyQrCode();
                break;
            case RiseMember.BUSINESS_THOUGHT:
                qrCodeUrl = ConfigUtils.getBusinessThoughtApplyQrCode();
                break;
            default:
                ;
        }
        return qrCodeUrl;
    }

}
