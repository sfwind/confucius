package com.iquanwai.confucius.biz.domain.course.signup;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.RedisUtil;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.course.*;
import com.iquanwai.confucius.biz.dao.fragmentation.*;
import com.iquanwai.confucius.biz.dao.wx.CallbackDao;
import com.iquanwai.confucius.biz.dao.wx.QuanwaiOrderDao;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.customer.CustomerMessageService;
import com.iquanwai.confucius.biz.domain.weixin.message.template.TemplateMessageService;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.common.customer.CourseReductionActivity;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampOrder;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampSchedule;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import com.iquanwai.confucius.biz.po.fragmentation.RiseCourseOrder;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.po.fragmentation.RiseOrder;
import com.iquanwai.confucius.biz.po.systematism.ClassMember;
import com.iquanwai.confucius.biz.po.systematism.CourseIntroduction;
import com.iquanwai.confucius.biz.po.systematism.CourseOrder;
import com.iquanwai.confucius.biz.po.systematism.QuanwaiClass;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
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
import java.util.stream.Collectors;

/**
 * Created by justin on 16/9/10.
 */
@Service
public class SignupServiceImpl implements SignupService {
    @Autowired
    private ClassDao classDao;
    @Autowired
    private RiseClassMemberDao riseClassMemberDao;
    @Autowired
    private CourseIntroductionDao courseIntroductionDao;
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
    private TemplateMessageService templateMessageService;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private RiseMemberTypeRepo riseMemberTypeRepo;
    @Autowired
    private RiseOrderDao riseOrderDao;
    @Autowired
    private RiseMemberCountRepo riseMemberCountRepo;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private ProblemDao problemDao;
    @Autowired
    private MonthlyCampOrderDao monthlyCampOrderDao;
    @Autowired
    private RiseCourseOrderDao riseCourseOrderDao;
    @Autowired
    private CallbackDao callbackDao;
    @Autowired
    private MessageService messageService;
    @Autowired
    private RestfulHelper restfulHelper;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private CourseReductionService courseReductionService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    private int PROBLEM_MAX_LENGTH = 30; //小课最长开放时间

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 支付二维码的高度
     */
    private final static int QRCODE_HEIGHT = 200;
    /**
     * 支付二维码的宽度
     */
    private final static int QRCODE_WIDTH = 200;
    /**
     * 小课训练营购买之后送的优惠券
     */
    private final static double MONTHLY_CAMP_COUPON = 100;
    /**
     * 购买会员赠送线下工作坊券
     */
    private final static double RISEMEMBER_OFFLINE_COUPON = 50;

    /**
     * 每个班级的当前学号
     */
    private Map<Integer, Integer> memberCount = Maps.newConcurrentMap();

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
    public Pair<Integer, String> riseMemberSignupCheck(Integer profileId, Integer memberTypeId) {
        return riseMemberCountRepo.prepareSignup(profileId);
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
            if (profile.getRiseMember() == 1 && (RiseMember.HALF_ELITE == riseMember.getMemberTypeId() || RiseMember.ELITE == riseMember.getMemberTypeId())) {
                right = "您已经是商学院会员";
            } else {
                // 检查权限
                boolean check = accountService.hasPrivilegeForBusinessSchool(profileId);
                if (check) {
                    left = 1;
                } else {
                    right = "您需要先申请商学院报名权限";
                }
            }
        } else if (memberTypeId == RiseMember.MONTHLY_CAMP) {
            // 购买小课训练营
            if (profile.getRiseMember() == 1 && (RiseMember.PROFESSIONAL == riseMember.getMemberTypeId() || RiseMember.HALF_PROFESSIONAL == riseMember.getMemberTypeId())) {
                right = "您已经是商学院会员";
            } else {
                if (profile.getRiseMember() == 3) {
                    right = "您已经是小课训练营用户";
                } else if (!ConfigUtils.getMonthlyCampOpen()) {
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
        Profile profile = profileDao.load(Profile.class, profileId);
        MemberType memberType = riseMemberTypeRepo.memberType(memberTypeId);
        Pair<String, Double> orderPair = generateOrderId(memberType.getFee(), couponId);

        Assert.notNull(profile, "用户信息错误");
        Assert.notNull(memberType, "会员类型错误");
        QuanwaiOrder quanwaiOrder = this.createQuanwaiOrder(profile.getOpenid(),
                orderPair.getLeft(), memberType.getFee(), orderPair.getRight(),
                memberTypeId + "", memberType.getName(), QuanwaiOrder.FRAG_MEMBER);

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
    public QuanwaiOrder signupMonthlyCamp(Integer profileId, Integer memberTypeId, Integer couponId) {
        // 如果是购买训练营小课，配置 zk，查看当前月份
        Profile profile = profileDao.load(Profile.class, profileId);
        MemberType memberType = riseMemberTypeRepo.memberType(memberTypeId);
        Assert.notNull(profile, "用户不能为空");
        Assert.notNull(memberType, "会员类型错误");
        Double fee = ConfigUtils.getMonthlyCampFee();
        Pair<String, Double> orderPair = generateOrderId(fee, couponId);

        QuanwaiOrder quanwaiOrder = createQuanwaiOrder(profile.getOpenid(),
                orderPair.getLeft(), fee, orderPair.getRight(),
                memberTypeId + "", ConfigUtils.getMonthlyCampMonth() + "月训练营", QuanwaiOrder.FRAG_CAMP);

        // 插入小课训练营报名数据
        MonthlyCampOrder monthlyCampOrder = new MonthlyCampOrder();
        monthlyCampOrder.setOrderId(orderPair.getLeft());
        monthlyCampOrder.setOpenId(profile.getOpenid());
        monthlyCampOrder.setProfileId(profileId);
        monthlyCampOrder.setMonth(ConfigUtils.getMonthlyCampMonth());
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
    public QuanwaiClass getCachedClass(Integer classId) {
        if (classMap.get(classId) == null || classMap.get(classId).get() == null) {
            QuanwaiClass quanwaiClass = classDao.load(QuanwaiClass.class, classId);
            if (quanwaiClass != null) {
                classMap.put(classId, new SoftReference<>(quanwaiClass));
            }
        }
        return classMap.get(classId).get();
    }

    @Override
    public CourseIntroduction getCachedCourse(Integer courseId) {
        if (courseMap.get(courseId) == null) {
            CourseIntroduction course = courseIntroductionDao.getByCourseId(courseId);
            if (course != null) {
                courseMap.put(courseId, course);
            }
        }
        return courseMap.get(courseId);
    }

    @Override
    public CourseOrder getOrder(String orderId) {
        return courseOrderDao.loadOrder(orderId);
    }

    @Override
    public void payMonthlyCampSuccess(String orderId) {
        MonthlyCampOrder campOrder = monthlyCampOrderDao.loadCampOrder(orderId);
        Assert.notNull(campOrder, "训练营购买订单不能为空，orderId：" + orderId);
        Integer profileId = campOrder.getProfileId();
        // 更新 profile 表中状态
        Profile profile = accountService.getProfile(profileId);
        profileDao.becomeMonthlyCampMember(profileId);

        // 清除历史 RiseMember 数据
        RiseClassMember delClassMember = riseClassMemberDao.queryByProfileId(profileId);
        if (delClassMember != null) {
            riseClassMemberDao.del(delClassMember.getId());
        }

        // RiseMember 新增记录
        String memberId = generateMemberId();
        RiseClassMember classMember = new RiseClassMember();
        classMember.setClassId(ConfigUtils.getMonthlyCampClassId());
        classMember.setClassName(ConfigUtils.getMonthlyCampClassId());
        classMember.setProfileId(profileId);
        classMember.setMemberId(memberId);
        classMember.setActive(1);
        riseClassMemberDao.insert(classMember);

        // 每当在 RiseMember 表新增一种状态时候，预先在 RiseMember 表中其他数据置为过期
        riseMemberDao.updateExpiredAhead(profileId);
        // 添加会员表
        RiseMember riseMember = new RiseMember();
        riseMember.setOpenId(campOrder.getOpenId());
        riseMember.setOrderId(campOrder.getOrderId());
        riseMember.setProfileId(campOrder.getProfileId());
        riseMember.setMemberTypeId(RiseMember.MONTHLY_CAMP);
        Date endDate = ConfigUtils.getMonthlyCampCloseDate();
        riseMember.setExpireDate(endDate);
        riseMemberDao.insert(riseMember);

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

        sendPurchaseMessage(profile, RiseMember.MONTHLY_CAMP, orderId);
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
    public String generateMemberId() {
        StringBuilder targetMemberId = new StringBuilder();

        String prefix = ConfigUtils.getMemberIdPrefix();

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

    private Integer createPlan(RiseCourseOrder riseCourseOrder) {
        Callback callback = callbackDao.loadUserCallback(riseCourseOrder.getOpenid());
        if (callback == null) {
            logger.error("报名小课异常，没有callback数据,orderId:{}", riseCourseOrder.getOrderId());
            messageService.sendAlarm("报名模块出错", "付费回调接口异常", "高", "订单id:" + riseCourseOrder.getOrderId(), "该用户没有Callback数据");
            return -1;
        }
        String cookieName;
        String cookieValue;
        if (callback.getAccessToken() != null) {
            cookieName = OAuthService.ACCESS_TOKEN_COOKIE_NAME;
            cookieValue = callback.getAccessToken();
        } else {
            cookieName = OAuthService.QUANWAI_TOKEN_COOKIE_NAME;
            cookieValue = callback.getPcAccessToken();
        }
        try {
            String body = restfulHelper.risePlanChoose(cookieName, cookieValue, riseCourseOrder.getProblemId());
            if (StringUtils.isEmpty(body)) {
                logger.error("调用rise生成小课接口异常");
                messageService.sendAlarm("报名模块出错", "生成小课接口异常", "高", "订单id:" + riseCourseOrder.getOrderId(), "返回体响应为空 ");
                return -1;
            } else {
                JSONObject result = JSONObject.parseObject(body);
                if (200 == result.getInteger("code")) {
                    return Integer.valueOf(result.get("msg").toString());
                } else {
                    messageService.sendAlarm("报名模块出错", "生成小课接口异常", "高", "返回code异常 \n订单id:" + riseCourseOrder.getOrderId(), result.toJSONString());
                    return -1;
                }
            }
        } catch (Exception e) {
            messageService.sendAlarm("报名模块出错", "生成小课接口异常", "高", "riseCourseEntry方法异常\n订单id:" + riseCourseOrder.getOrderId(), e.getLocalizedMessage());
            return -1;
        }
    }

    @Override
    public void riseMemberEntry(String orderId) {
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
        Date expireDate;
        if (RiseMember.ELITE == memberType.getId()) {
            //查看有没有老的
            RiseMember exist = riseMemberDao.loadValidRiseMember(riseOrder.getProfileId());
            if (exist != null) {
                // 升级
                expireDate = DateUtils.afterNatureMonths(exist.getExpireDate(), 12);
            } else {
                expireDate = DateUtils.afterNatureMonths(new Date(), 12);
            }
            //精英会员一年
            profileDao.becomeRiseEliteMember(openId);

            // 清除历史 RiseMember 数据
            RiseClassMember delMember = riseClassMemberDao.queryByProfileId(riseOrder.getProfileId());
            if (delMember != null) {
                riseClassMemberDao.del(delMember.getId());
            }

            // RiseMember 新增记录
            String memberId = generateMemberId();
            RiseClassMember classMember = new RiseClassMember();
            classMember.setClassId(ConfigUtils.getRiseMemberClassId());
            classMember.setClassName(ConfigUtils.getRiseMemberClassId());
            classMember.setProfileId(riseOrder.getProfileId());
            classMember.setMemberId(memberId);
            classMember.setActive(1);
            riseClassMemberDao.insert(classMember);
        } else {
            logger.error("该会员ID异常{}", memberType);
            messageService.sendAlarm("报名模块出错", "会员id异常",
                    "高", "订单id:" + orderId, "会员类型异常");
            return;
        }
        // 如果存在，则将已经存在的 riseMember 数据置为已过期
        riseMemberDao.updateExpiredAhead(riseOrder.getProfileId());
        // 添加会员表
        RiseMember riseMember = new RiseMember();
        riseMember.setOpenId(riseOrder.getOpenid());
        riseMember.setOrderId(riseOrder.getOrderId());
        riseMember.setProfileId(riseOrder.getProfileId());
        riseMember.setMemberTypeId(memberType.getId());
        riseMember.setExpireDate(expireDate);
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
        Profile profile = profileDao.queryByOpenId(openId);
        // 发送模板消息
        sendPurchaseMessage(profile, memberType.getId(), orderId);
    }

    private void sendPurchaseMessage(Profile profile, Integer memberTypeId, String orderId) {
        Assert.notNull(profile, "openid不能为空");
        logger.info("发送欢迎消息给付费用户{}", profile.getOpenid());
        boolean isFull = profile.getIsFull() == 1;
        boolean isBindMobile = profile.getMobileNo() != null;
        String detailUrl = ConfigUtils.domainName() + "/rise/static/customer/profile?goRise=true";
        String mobileUrl = ConfigUtils.domainName() + "/rise/static/customer/mobile/check?goRise=true";
        String sendUrl = isFull ? isBindMobile ? null : mobileUrl : detailUrl;

        switch (memberTypeId) {
            case RiseMember.ELITE: {
                // 发送消息给一年精英版的用户
                customerMessageService.sendCustomerMessage(profile.getOpenid(), ConfigUtils.getValue("risemember.elite.pay.send.image"), Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
                if (sendUrl != null) {
                    messageService.sendMessage("点此完善个人信息，才能参加校友会，获取更多人脉资源喔！", Objects.toString(profile.getId()), MessageService.SYSTEM_MESSAGE, sendUrl);
                }
                break;
            }
            case RiseMember.MONTHLY_CAMP: {
                // 发送消息给小课训练营购买用户
                customerMessageService.sendCustomerMessage(profile.getOpenid(), ConfigUtils.getValue("risemember.monthly.camp.pay.send.image"), Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
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
    public RiseCourseOrder getRiseCourse(String orderId) {
        return riseCourseOrderDao.loadOrder(orderId);
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
        List<MemberType> memberTypes = riseMemberTypeRepo.memberTypes();
        memberTypes.forEach(item -> {
            item.setStartTime(DateUtils.parseDateToStringByCommon(new Date()));
            if (item.getId().equals(RiseMember.MONTHLY_CAMP)) {
                item.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(ConfigUtils.getMonthlyCampCloseDate(), 1)));
            } else {
                item.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(DateUtils.afterNatureMonths(new Date(), item.getOpenMonth()), 1)));
            }
        });
        return memberTypes;
    }

    @Override
    public Double calculateMemberCoupon(Integer memberTypeId, List<Integer> couponIdGroup) {
        Double amount = couponIdGroup.stream().map(couponId -> costRepo.getCoupon(couponId)).filter(Objects::nonNull).mapToDouble(Coupon::getAmount).sum();
        MemberType memberType = riseMemberTypeRepo.memberType(memberTypeId);
        if (memberType.getFee() >= amount) {
            return CommonUtils.substract(memberType.getFee(), amount);
        } else {
            return 0D;
        }
    }

    @Override
    public Double calculateCampCoupon(Integer profileId, Integer couponId) {
        logger.info("用户 id: {}", profileId);
        logger.info("优惠券 id: {}", couponId);
        Coupon coupon = couponDao.load(Coupon.class, couponId);
        Assert.isTrue(profileId.equals(coupon.getProfileId()), "当前尚未拥有此优惠券");
        Double fee = ConfigUtils.getMonthlyCampFee();
        if (fee >= coupon.getAmount()) {
            return CommonUtils.substract(fee, coupon.getAmount());
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
    public Integer loadCurrentCampMonth() {
        return ConfigUtils.getMonthlyCampMonth();
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
        if (riseMember != null) {
            switch (riseMember.getMemberTypeId()) {
                case RiseMember.ELITE:
                case RiseMember.HALF_ELITE:
                    return null;
                case RiseMember.PROFESSIONAL:
                    fee = CommonUtils.substract(memberType.getFee(), 880d);
                    businessSchool.setInitDiscount(880d);
                    break;
                case RiseMember.HALF_PROFESSIONAL:
                    fee = CommonUtils.substract(memberType.getFee(), 580d);
                    businessSchool.setInitDiscount(580d);
                    break;
                case RiseMember.MONTHLY_CAMP:
                    fee = memberType.getFee();
                    break;
                default:
                    fee = memberType.getFee();
            }
            // 计算结束时间
            Date endTime = DateUtils.afterNatureMonths(riseMember.getExpireDate(), 12);
            businessSchool.setEndTime(DateUtils.parseDateToStringByCommon(endTime));

        } else {
            fee = memberType.getFee();
            businessSchool.setEndTime(memberType.getEndTime());

        }
        businessSchool.setFee(fee);
        businessSchool.setStartTime(DateUtils.parseDateToStringByCommon(new Date()));

        return businessSchool;
    }


    //生成学号 2位课程号2位班级号3位学号
    private String memberId(Integer courseId, Integer classId) {
        Integer classNumber = classDao.load(QuanwaiClass.class, classId).getClassNumber();
        Integer memberNumber = getMemberNumber(classId);
        return String.format("%02d%02d%03d", courseId, classNumber, memberNumber);
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

    private String payUrl(String productId) {
        String nonce_str = CommonUtils.randomString(10);
        String time_stamp = String.valueOf(DateUtils.currentTimestamp());
        String appid = ConfigUtils.getAppid();
        String mch_id = ConfigUtils.getMch_id();

        Map<String, String> map = Maps.newHashMap();
        map.put("nonce_str", nonce_str);
        map.put("time_stamp", time_stamp);
        map.put("appid", appid);
        map.put("mch_id", mch_id);
        map.put("product_id", productId);
        //生成签名
        String sign = CommonUtils.sign(map);
        map.put("sign", sign);

        return CommonUtils.placeholderReplace(PAY_URL, map);
    }

    private synchronized Integer getMemberNumber(Integer classId) {
        if (memberCount.get(classId) == null) {
            int number = classMemberDao.classMemberNumber(classId);
            memberCount.put(classId, number + 1);
            return number + 1;
        }

        int count = memberCount.get(classId) + 1;
        memberCount.put(classId, count);
        return count;
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
            List<Coupon> coupons = couponIdGroup.stream().map(couponId -> costRepo.getCoupon(couponId)).collect(Collectors.toList());
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

    private Double getCoursePrice(Integer profileId, Integer problemId) {
        Double fee = ConfigUtils.getRiseCourseFee();
        CourseReductionActivity activity = courseReductionService.loadRecentCourseReduction(profileId, problemId);
        if (activity != null && activity.getPrice() != null) {
            fee = activity.getPrice();
        }
        return fee;
    }

}
