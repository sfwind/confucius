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
import com.iquanwai.confucius.biz.domain.fragmentation.ClassMember;
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
import com.iquanwai.confucius.biz.po.fragmentation.course.BusinessSchoolConfig;
import com.iquanwai.confucius.biz.po.fragmentation.course.CourseConfig;
import com.iquanwai.confucius.biz.po.systematism.CourseIntroduction;
import com.iquanwai.confucius.biz.po.systematism.QuanwaiClass;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQPublisher;
import org.apache.commons.lang3.StringUtils;
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
 * Created by justin on 2018/4/26.
 */
@Service
public class EntryManagerImpl implements EntryManager {
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private OperateRotateDao operateRotateDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private BusinessSchoolApplicationDao businessSchoolApplicationDao;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private ShortMessageService shortMessageService;
    @Autowired
    private FragmentClassMemberDao fragmentClassMemberDao;
    @Autowired
    private BusinessSchoolApplicationOrderDao businessSchoolApplicationOrderDao;
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private MessageService messageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private QuanwaiOrderDao quanwaiOrderDao;
    @Autowired
    private MemberTypeManager memberTypeManager;
    @Autowired
    private RiseOrderDao riseOrderDao;
    @Autowired
    private MonthlyCampOrderDao monthlyCampOrderDao;
    @Autowired
    private CostManger costManger;
    @Autowired
    private ProfileDao profileDao;

    private static final String RISEMEMBER_OPERATEROTATE_SCENE_CODE = "rise_member_pay_success";
    private static final String MONTHLYCAMP_OPERATEROTATE_SCENE_CODE = "monthly_camp_pay_success";
    private static final int OPERATEROTATE_SWITCH_SIZE = 200;
    private static String PLACEHOLDER = "{prefix}";
    private static String CLASS_MEMBER_ID_LOCK = "lock:class:member:" + PLACEHOLDER;
    private static String CLASS_NAME_KEY = "class:name:num:" + PLACEHOLDER;
    private static String CLASS_MEMBER_ID_KEY = "member:id:num:" + PLACEHOLDER;

    private static Map<Integer, String> classMemberPrefixes = Maps.newHashMap();

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, SoftReference<QuanwaiClass>> classMap = Maps.newHashMap();
    private Map<Integer, CourseIntroduction> courseMap = Maps.newHashMap();

    private RabbitMQPublisher rabbitMQPublisher;
    private RabbitMQPublisher paySuccessPublisher;
    private RabbitMQPublisher freshLoginUserPublisher;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    /**
     * 初始化缓存
     */
    @PostConstruct
    public void init() {
        classMap.clear();
        courseMap.clear();
        paySuccessPublisher = rabbitMQFactory.initFanoutPublisher(com.iquanwai.confucius.biz.domain.weixin.pay.PayService.RISE_PAY_SUCCESS_TOPIC);
        freshLoginUserPublisher = rabbitMQFactory.initFanoutPublisher(com.iquanwai.confucius.biz.domain.weixin.pay.PayService.LOGIN_USER_RELOAD_TOPIC);
        rabbitMQPublisher = rabbitMQFactory.initFanoutPublisher("camp_order_topic");

    }

    private final static int PROBLEM_MAX_LENGTH = 30; //课程最长开放时间

    static {
        classMemberPrefixes.put(RiseMember.ELITE, "C");
        classMemberPrefixes.put(RiseMember.BUSINESS_THOUGHT, "S");
        classMemberPrefixes.put(RiseMember.CAMP, "M");
    }

    @Override
    public void payApplicationSuccess(String orderId) {
        BusinessSchoolApplicationOrder order = businessSchoolApplicationOrderDao.loadBusinessSchoolApplicationOrder(orderId);
        Assert.notNull(order, "商学院申请购买订单不能为空，orderId：" + orderId);

        QuanwaiOrder quanwaiOrder = quanwaiOrderDao.loadOrder(orderId);
        Integer membetTypeId = riseMemberManager.loadWannaGoodsIdByApplyId(Integer.valueOf(quanwaiOrder.getGoodsId())).getRight();
        BusinessSchoolApplication apply = businessSchoolApplicationDao.loadLatestInvalidApply(order.getProfileId(), membetTypeId);
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
    public void payMonthlyCampSuccess(String orderId) {
        MonthlyCampOrder campOrder = monthlyCampOrderDao.loadCampOrder(orderId);
        Assert.notNull(campOrder, "专项课购买订单不能为空，orderId：" + orderId);

        Integer profileId = campOrder.getProfileId();

        // 更新 profile 表中状态
        Profile profile = accountService.getProfile(profileId);

        // ClassMember 新增记录
        insertClassMemberMemberId(profileId, RiseMember.CAMP);

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

        CourseConfig courseConfig = cacheService.loadCourseConfig(RiseMember.CAMP);
        Integer year = courseConfig.getSellingYear();
        Integer month = courseConfig.getSellingMonth();

        sendPurchaseMessage(profile, RiseMember.CAMP, orderId, year, month);
        // 更新优惠券使用状态
        QuanwaiOrder quanwaiOrder = quanwaiOrderDao.loadOrder(orderId);
        if (quanwaiOrder.getDiscount() != 0.0) {
            logger.info("{}使用优惠券", profile.getOpenid());
            costManger.updateCoupon(Coupon.USED, orderId);
        }
        // 刷新相关状态
//        refreshStatus(orderId);
    }

    @Override
    public void payRiseSuccess(String orderId) {
        /**
         * 1.查看是否已经处理过
         * 2.memberTypeId是否属于该GoodsType
         * 3.进行中的plan加点评
         * 4.1如果不存在老的，则正常时间
         * 4.2如果存在老的，则升级延期，过期老的
         * 5.插入班级学号
         * 6.生成课表在platon
         */
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
        MemberType memberType = memberTypeManager.memberType(riseOrder.getMemberType());
        // 开课时间
        BusinessSchoolConfig businessSchoolConfig = cacheService.loadBusinessCollegeConfig(memberType.getId());
        // 查看是否存在现成会员数据
        // 添加会员表
        RiseMember riseMember = new RiseMember();
        riseMember.setOrderId(riseOrder.getOrderId());
        riseMember.setProfileId(riseOrder.getProfileId());
        riseMember.setMemberTypeId(memberType.getId());
        riseMember.setExpired(false);
        riseMember.setVip(false);
        // 学习时间
        Integer learningMonthDate = 12;
        if (memberType.getOpenMonth() != null) {
            learningMonthDate = memberType.getOpenMonth();
        } else {
            logger.error("该会员ID异常{}", memberType);
            messageService.sendAlarm("报名模块出错", "会员id异常", "高", "订单id:" + orderId, "会员类型异常");
        }

        // 所有计划设置为会员
        List<ImprovementPlan> plans = improvementPlanDao.loadAllPlans(riseOrder.getProfileId());
        plans.stream().filter(plan -> !plan.getRiseMember()).forEach(plan -> {
            // 不是会员的计划，设置一下
            plan.setCloseDate(DateUtils.afterDays(new Date(), PROBLEM_MAX_LENGTH));
            if (plan.getStatus().equals(ImprovementPlan.RUNNING) && (memberType.getId().equals(RiseMember.ELITE) || memberType.getId().equals(RiseMember.HALF_ELITE))) {
                // 给精英版正在进行的 plan + 1 个求点评次数
                improvementPlanDao.becomeRiseEliteMember(plan);
            } else {
                // 非精英版或者不是正在进行的，不加点评次数
                improvementPlanDao.becomeRiseMember(plan);
            }
        });

        // TODO 专业版测试,以后删除
        RiseMember existRiseMember = riseMemberDao.loadValidRiseMemberByMemberTypeId(riseOrder.getProfileId(),
                Lists.newArrayList(RiseMember.HALF, RiseMember.ANNUAL)).stream().findFirst().orElse(null);
        if (existRiseMember == null) {
            // 非续费，查询本次开营时间
            riseMember.setOpenDate(businessSchoolConfig.getOpenDate());
            riseMember.setExpireDate(DateUtils.afterMonths(businessSchoolConfig.getOpenDate(), learningMonthDate));
            profileDao.initOnceRequestCommentCount(riseOrder.getProfileId());
        } else {
            // 如果存在，则将已经存在的 riseMember 数据置为已过期
            riseMemberDao.expired(existRiseMember.getId());
            riseMember.setExpireDate(DateUtils.afterMonths(existRiseMember.getExpireDate(), learningMonthDate));
//            // 续费，继承OpenDate
            riseMember.setOpenDate(existRiseMember.getOpenDate());
        }
        // 插入班级、学号（学号幂等）
        insertClassMemberMemberId(riseOrder.getProfileId(), riseOrder.getMemberType());
        riseMemberDao.insert(riseMember);

        Profile profile = accountService.getProfile(riseOrder.getProfileId());
        // 回写支付状态
        BusinessSchoolApplication businessSchoolApplication = businessSchoolApplicationDao.loadLastApproveApplication(riseOrder.getProfileId(), riseOrder.getMemberType());
        if (businessSchoolApplication != null) {
            businessSchoolApplicationDao.entryApply(businessSchoolApplication.getId());
        }

        // 发送模板消息
        sendPurchaseMessage(profile, memberType.getId(), orderId, businessSchoolConfig.getSellingYear(), businessSchoolConfig.getSellingMonth());
        QuanwaiOrder quanwaiOrder = quanwaiOrderDao.loadOrder(orderId);
        if (quanwaiOrder.getDiscount() != 0.0) {
            logger.info("{}使用优惠券", profile.getOpenid());
            costManger.updateCoupon(Coupon.USED, orderId);
        }
        this.refreshStatus(orderId);
    }

    /**
     * 生成memberId以及插入ClassMember
     * 新学号格式：字母前缀 + 四位年月（1701）+ 两位班级序号  + 三位递增唯一序列（1701011001）
     *
     * @param profileId    用户id
     * @param memberTypeId 会员id
     */
    public void insertClassMemberMemberId(Integer profileId, Integer memberTypeId) {
        // 获取售卖年、月
        int sellingYear;
        int sellingMonth;
        String prefix;
        try {
            Pair<Integer, Integer> yearMonth = getMemberSellingInfo(memberTypeId);
            Assert.notNull(yearMonth, "获取售卖年月失败");
            sellingYear = yearMonth.getLeft();
            sellingMonth = yearMonth.getRight();
            // 获取prefix
            String classPrefix = String.format("%02d", sellingYear % 2000) + String.format("%02d", sellingMonth);
            prefix = classMemberPrefixes.get(memberTypeId) + classPrefix;
            Assert.notNull(prefix, "获取班级前缀失败");
        } catch (Exception e) {
            // 数据异常
            messageService.sendAlarm("报名模块次级异常", "memberTypeId:" + memberTypeId,
                    "中", "profileId:" + profileId, e.getLocalizedMessage());
            return;
        }

        // 班序号key、班内序号key、班级前缀
        String classNameKey = StringUtils.replace(CLASS_NAME_KEY, PLACEHOLDER, prefix);
        String memberIdKey = StringUtils.replace(CLASS_MEMBER_ID_KEY, PLACEHOLDER, prefix);
        StringBuilder targetMemberId = new StringBuilder();
        targetMemberId.append(prefix);
        StringBuilder targetClassName = new StringBuilder();
        List<ClassMember> existsClasses = fragmentClassMemberDao.loadByProfileIdAndMemberTypeId(profileId, memberTypeId);
        redisUtil.lock(StringUtils.replace(CLASS_MEMBER_ID_LOCK, PLACEHOLDER, prefix), (lock) -> {
            // 1.班级序号
            String classNameSequence = redisUtil.get(classNameKey);
            Integer className;
            if (classNameSequence == null) {
                className = 1;
                classNameSequence = "01";
            } else {
                className = Integer.parseInt(classNameSequence);
            }
            // 2.班内序号
            String memberIdSequence = redisUtil.get(memberIdKey);
            Integer memberId;
            if (memberIdSequence == null) {
                memberId = 1;
                memberIdSequence = "001";
            } else {
                memberId = Integer.parseInt(memberIdSequence) + 1;
                memberIdSequence = String.format("%03d", memberId);
            }

            // 设置班级和序号
            targetMemberId.append(classNameSequence);
            targetMemberId.append(memberIdSequence);
            targetClassName.append(prefix);
            targetClassName.append(classNameSequence);
            if (memberId >= 200) {
                // 满200人，人数重置1，班级+1
                redisUtil.set(memberIdKey, "001", TimeUnit.DAYS.toSeconds(60));
                redisUtil.set(classNameKey, String.format("%02d", className + 1), TimeUnit.DAYS.toSeconds(60));
            } else {
                // 不满200，人数更新
                redisUtil.set(memberIdKey, memberIdSequence, TimeUnit.DAYS.toSeconds(60));
            }

        });

        boolean existsClassName = existsClasses.stream().anyMatch(item -> Objects.equals(targetClassName.toString(), item.getClassName()));

        Profile profile = accountService.getProfile(profileId);
        boolean hasMemberId = profile.getMemberId() != null;
        String memberId;
        if (!hasMemberId) {
            memberId = targetMemberId.toString();
            accountService.updateMemberId(profileId, memberId);
        }
        if (!existsClassName) {
            ClassMember classMember = new ClassMember();
            classMember.setClassName(targetClassName.toString());
            classMember.setMemberTypeId(memberTypeId);
            classMember.setProfileId(profileId);
            fragmentClassMemberDao.insert(classMember);
        }
        logger.info(targetClassName.toString() + ":" + targetMemberId.toString() + ":" + profile.getMemberId());
    }

    /**
     * 生成会员售卖年月信息
     *
     * @param memberTypeId 会员id
     * @return left：year <br/>
     * right:month
     */
    private Pair<Integer, Integer> getMemberSellingInfo(Integer memberTypeId) {
        CourseConfig courseConfig = cacheService.loadCourseConfig(memberTypeId);
        if (courseConfig != null) {
            return Pair.of(courseConfig.getSellingYear(), courseConfig.getSellingMonth());
        } else {
            return null;
        }
    }

    /**
     * 购买完专项课之后，更新 RiseMember 表中的数据
     *
     * @param profile 用户 Profile
     */
    private void updateMonthlyCampRiseMemberStatus(Profile profile, String orderId) {
        CourseConfig monthlyCampConfig = cacheService.loadCourseConfig(RiseMember.CAMP);
        // 每当在 RiseMember 表新增一种状态时候，预先在 RiseMember 表中其他数据置为过期
        RiseMember existRiseMember = riseMemberManager.oldMember(profile.getId());
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
                // List<OperateRotate> riseMemberOperateRotates = operateRotates.stream()
                //         .filter(operateRotate -> RISEMEMBER_OPERATEROTATE_SCENE_CODE.equals(operateRotate.getSceneCode()))
                //         .sorted(Comparator.comparingInt(OperateRotate::getSequence))
                //         .collect(Collectors.toList());

                // redisUtil.lock("operateRotate:riseMember:paySuccess", lock -> {
                // String riseMemberKey = "operateRotate:" + RISEMEMBER_OPERATEROTATE_SCENE_CODE + ":index";
                // String riseMemberIndexStr = redisUtil.get(riseMemberKey);
                // int riseMemberIndex = riseMemberIndexStr == null ? 1 : Integer.parseInt(riseMemberIndexStr);
                // redisUtil.set(riseMemberKey, riseMemberIndex + 1);
                // logger.info("riseMemberIndex: {}", riseMemberIndex);
                // int sequence = riseMemberIndex % OPERATEROTATE_SWITCH_SIZE == 0 ? riseMemberIndex / OPERATEROTATE_SWITCH_SIZE : riseMemberIndex / OPERATEROTATE_SWITCH_SIZE + 1;
                // logger.info("sequence: {}", sequence);
                // OperateRotate operateRotate = riseMemberOperateRotates.get(sequence % riseMemberOperateRotates.size() == 0 ? riseMemberOperateRotates.size() - 1 : sequence % riseMemberOperateRotates.size() - 1);
                // Assert.notNull(operateRotate);
                // logger.info("operateRotate mediaId: {}", operateRotate.getMediaId());

                String entryCode = profile.getMemberId();
                logger.info("发送会员数据");
                // 发送消息给一年精英版的用户
                customerMessageService.sendCustomerMessage(profile.getOpenid(), ConfigUtils.getHeadTeacherWeiWeiMediaId(), Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
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
                    shortMessage.setContent(profile.getNickname() + " 你好，欢迎加入商业进阶课。请添加你的班主任微信：MBAELLA，接下来班主任将帮助你更好地学习。" +
                            "快回复你的学号（学号：" + entryCode + "）向班主任报道吧！");
                    shortMessage.setNickname(shortMessage.getNickname());
                    shortMessage.setType(ShortMessage.BUSINESS);
                    shortMessage.setPhone(profile.getMobileNo());
                    shortMessageService.sendMessage(shortMessage);
                }
                // });
                break;
            }
            default: {
                messageService.sendAlarm("报名模块出错", "报名后发送消息", "中",
                        "订单id:" + orderId + "\nprofileId:" + profile.getId(), "会员类型异常");
            }
        }
    }

    private void refreshStatus(String orderId) {
        QuanwaiOrder quanwaiOrder = quanwaiOrderDao.loadOrder(orderId);
        Profile profile = accountService.getProfile(quanwaiOrder.getProfileId());
        // 刷新会员状态
        try {
            freshLoginUserPublisher.publish(profile.getUnionid());
        } catch (ConnectException e) {
            logger.error("发送会员信息更新mq失败", e);
        }
        // 发送支付成功 mq 消息
        try {
            logger.info("发送支付成功message:{}", quanwaiOrder);
            paySuccessPublisher.publish(quanwaiOrder);
        } catch (ConnectException e) {
            logger.error("发送支付成功mq失败", e);
            messageService.sendAlarm("报名模块出错", "发送支付成功mq失败", "高", "订单id:" + quanwaiOrder.getOrderId(),
                    e.getLocalizedMessage());
        }
    }
}
