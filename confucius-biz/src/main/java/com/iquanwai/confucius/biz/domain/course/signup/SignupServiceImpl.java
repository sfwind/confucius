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
import com.iquanwai.confucius.biz.domain.course.progress.CourseStudyService;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.customer.CustomerMessageService;
import com.iquanwai.confucius.biz.domain.weixin.message.template.TemplateMessage;
import com.iquanwai.confucius.biz.domain.weixin.message.template.TemplateMessageService;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.common.customer.CourseReductionActivity;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.po.systematism.ClassMember;
import com.iquanwai.confucius.biz.po.systematism.Course;
import com.iquanwai.confucius.biz.po.systematism.CourseIntroduction;
import com.iquanwai.confucius.biz.po.systematism.CourseOrder;
import com.iquanwai.confucius.biz.po.systematism.QuanwaiClass;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.QRCodeUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQPublisher;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.lang.ref.SoftReference;
import java.net.ConnectException;
import java.util.*;
import java.util.List;

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
    public Pair<Integer, Integer> signupCheck(Integer profileId, Integer courseId) {
        // 还没有正式进入班级
        return classMemberCountRepo.prepareSignup(profileId, courseId);
    }

    @Override
    public Pair<Integer, String> riseMemberSignupCheck(Integer profileId, Integer memberTypeId) {
        return riseMemberCountRepo.prepareSignup(profileId);
    }

    @Override
    public Pair<Integer, String> riseCourseSignupCheck(Integer profileId, Integer problemId) {
        RiseMember riseMember = riseMemberDao.validRiseMember(profileId);
        // Rise会员可以直接学习，页面上应该不会调用这个接口，以防万一
        if (riseMember != null) {
            return new MutablePair<>(-1, "您已经是RISE会员，无需单独购买小课");
        }
        return new MutablePair<>(1, "");
    }

    @Override
    public Pair<Integer, String> risePurchaseCheck(Integer profileId, Integer memberTypeId) {
        Profile profile = accountService.getProfile(profileId);
        Assert.notNull(profile, "用户不能为空");
        Integer left = -1;
        String right = "正常";
        if (memberTypeId == RiseMember.ELITE) {
            // 购买会员
            if (profile.getRiseMember() == 1) {
                right = "您已经是 RISE 会员";
            } else {
                left = 1;
            }
        } else if (memberTypeId == RiseMember.MONTHLY_CAMP) {
            // 购买小课训练营
            if (profile.getRiseMember() == 1) {
                right = "您已经是 RISE 会员";
            } else if (profile.getRiseMember() == 3) {
                right = "您已经是小课训练营用户";
            } else {
                left = 1;
            }
        }
        return new MutablePair<>(left, right);
    }

    @Override
    public Pair<Integer, String> riseMemberSignupCheckNoHold(Integer profileId, Integer memberTypeId) {
        return riseMemberCountRepo.prepareSignup(profileId, false);
    }

    @Override
    public QuanwaiOrder signupCourse(String openid, Integer profileId, Integer courseId, Integer classId) {
        //生成订单
        QuanwaiOrder quanwaiOrder = new QuanwaiOrder();
        quanwaiOrder.setCreateTime(new Date());
        quanwaiOrder.setOpenid(openid);
        //orderId 16位随机字符
        String orderId = CommonUtils.randomString(16);
        quanwaiOrder.setOrderId(orderId);
        CourseIntroduction course = getCachedCourse(courseId);
        if (course == null) {
            logger.error("courseId {} is not existed", courseId);
            return null;
        }
        double discount = 0.0;
        //计算优惠
        if (costRepo.hasCoupon(profileId)) {
            discount = costRepo.discount(course.getFee(), profileId, orderId);
        }
        quanwaiOrder.setTotal(course.getFee());
        quanwaiOrder.setDiscount(discount);
        quanwaiOrder.setPrice(CommonUtils.substract(course.getFee(), discount));
        quanwaiOrder.setStatus(QuanwaiOrder.UNDER_PAY); //待支付
        quanwaiOrder.setGoodsId(courseId + "");
        quanwaiOrder.setGoodsName(course.getCourseName());
        quanwaiOrder.setGoodsType(QuanwaiOrder.SYSTEMATISM);
        quanwaiOrderDao.insert(quanwaiOrder);

        //生成体系化报名数据
        CourseOrder courseOrder = new CourseOrder();
        courseOrder.setClassId(classId);
        courseOrder.setCourseId(courseId);
        courseOrder.setEntry(false);
        courseOrder.setIsDel(false);
        courseOrder.setOpenid(openid);
        courseOrder.setProfileId(profileId);
        courseOrder.setOrderId(orderId);
        courseOrderDao.insert(courseOrder);

        return quanwaiOrder;
    }

    @Override
    public QuanwaiOrder signupRiseMember(Integer profileId, Integer memberTypeId, Integer couponId) {
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
    public QuanwaiOrder signupRiseCourse(Integer profileId, Integer problemId, Integer couponId) {
        // 查询该openid 是否是我们的用户
        Profile profile = profileDao.load(Profile.class, profileId);
        Double fee = this.getCoursePrice(profileId, problemId);
        CourseReductionActivity activity = courseReductionService.loadRecentCourseReduction(profileId, problemId);
        if (activity != null && activity.getPrice() != null) {
            fee = activity.getPrice();
        }
        Problem problem = problemDao.load(Problem.class, problemId);
        Assert.notNull(problem, "小课数据异常");
        Assert.notNull(fee, "会员价格异常");
        Assert.notNull(profile, "用户信息不能为空");
        Pair<String, Double> orderPair = generateOrderId(fee, couponId);
        QuanwaiOrder quanwaiOrder = this.createQuanwaiOrder(profile.getOpenid(),
                orderPair.getLeft(), fee, orderPair.getRight(),
                problemId.toString(), problem.getProblem(), QuanwaiOrder.FRAG_COURSE);
        // 插入rise小课单卖的报名数据
        RiseCourseOrder riseCourseOrder = new RiseCourseOrder();
        riseCourseOrder.setProblemId(problemId);
        riseCourseOrder.setProfileId(profileId);
        riseCourseOrder.setOpenid(profile.getOpenid());
        riseCourseOrder.setOrderId(orderPair.getLeft());
        riseCourseOrderDao.insert(riseCourseOrder);
        return quanwaiOrder;
    }

    @Override
    public QuanwaiOrder signupMonthlyCamp(Integer profileId, Integer couponId) {
        // 如果是购买训练营小课，配置 zk，查看当前月份
        Profile profile = profileDao.load(Profile.class, profileId);
        Assert.notNull(profile, "用户不能为空");
        Double fee = ConfigUtils.getMonthlyCampFee();
        Pair<String, Double> orderPair = generateOrderId(fee, couponId);
        int goodsId = Constants.RISE_MEMBER.MONTHLY_CAMP;
        QuanwaiOrder quanwaiOrder = createQuanwaiOrder(profile.getOpenid(),
                orderPair.getLeft(), fee, orderPair.getRight(),
                Integer.toString(goodsId), ConfigUtils.getMonthlyCampMonth() + "月小课训练营",
                QuanwaiOrder.FRAG_CAMP);

        // 插入训练营小课报名数据
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
    public String payQRCode(String productId) {
        String payUrl = payUrl(productId);
        String path = "/data/static/images/qrcode/" + productId + ".jpg";
        String picUrl = ConfigUtils.resourceDomainName() + "/images/qrcode/" + productId + ".jpg";

        //生成二维码base64编码
        Image image = QRCodeUtils.genQRCode(payUrl, QRCODE_WIDTH, QRCODE_HEIGHT);
        if (image == null) {
            logger.error("二维码生成失败");
        } else {
            QRCodeUtils.image2FS(image, path);
        }

        return picUrl;
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
    public String entry(String orderId) {
        CourseOrder courseOrder = courseOrderDao.loadOrder(orderId);
        Integer classId = courseOrder.getClassId();
        Integer courseId = courseOrder.getCourseId();
        String openid = courseOrder.getOpenid();
        Integer profileId = courseOrder.getProfileId();
        //体系化订单标记已报名
        courseOrderDao.entry(orderId);
        ClassMember classMember = classMemberDao.getClassMember(classId, profileId);
        Date closeDate = getCloseDate(classId, courseId);
        if (classMember != null) {
            //已经毕业或者已经超过关闭时间,重置学员数据
            if (classMember.getGraduate() || classMember.getCloseDate().before(DateUtils.startDay(new Date()))) {
                classMemberDao.reEntry(classMember.getId(), closeDate);
            }
            //发送录取消息
            sendWelcomeMsg(courseId, openid, classId, classMember.getMemberId());
            return classMember.getMemberId();
        }
        classMember = new ClassMember();
        classMember.setClassId(classId);
        classMember.setOpenId(openid);
        classMember.setCourseId(courseId);
        //只有长课程有学号
        String memberId = "";
        if (getCachedCourse(courseId).getType() == Course.LONG_COURSE) {
            memberId = memberId(courseId, classId);
            classMember.setMemberId(memberId);
        }
        //设置课程关闭时间
        classMember.setCloseDate(closeDate);
        classMember.setProfileId(profileId);
        classMemberDao.entry(classMember);
        //发送录取消息
        sendWelcomeMsg(courseId, openid, classId, memberId);
        return memberId;
    }

    @Override
    public void riseCourseEntry(String orderId) {
        RiseCourseOrder riseCourseOrder = riseCourseOrderDao.loadOrder(orderId);
        // 用户购买小课逻辑
        Integer profileId = riseCourseOrder.getProfileId();
        riseCourseOrderDao.entry(orderId);
        // 检查用户是不是已经学过这个小课
        ImprovementPlan plan = improvementPlanDao.loadPlanByProblemId(profileId, riseCourseOrder.getProblemId());
        if (plan == null) {
            // 用户没有学过这个小课，生成他
            // TODO 这里不能改成异步mq，因为要等待rise返回planId
            Integer planId = createPlan(riseCourseOrder);
            if (planId < 0) {
                logger.error("报名小课异常，返回的planId异常");
            } else {
                improvementPlanDao.reOpenPlan(planId, DateUtils.afterDays(new Date(), PROBLEM_MAX_LENGTH));
            }
        } else {
            // 用户有这个小课
            if (plan.getStatus() == ImprovementPlan.TRIALCLOSE) {
                // 试用结束，设置成正在进行，延长三十天
                improvementPlanDao.reOpenPlan(plan.getId(), DateUtils.afterDays(new Date(), PROBLEM_MAX_LENGTH));
            } else {
                logger.error("报名小课异常，小课状态为:{}", plan.getStatus());
                messageService.sendAlarm("报名模块出错", "用户小课数据异常",
                        "高", "订单id:" + orderId, "该用户购买的小课，状态并不是使用结束");
            }
        }
    }

    @Override
    public void payMonthlyCampSuccess(String orderId) {
        MonthlyCampOrder campOrder = monthlyCampOrderDao.loadCampOrder(orderId);
        Assert.notNull(campOrder, "训练营购买订单不能为空，orderId：" + orderId);
        Integer profileId = campOrder.getProfileId();
        // 更新 profile 表中状态
        Profile profile = accountService.getProfile(profileId);
        profileDao.becomeMonthlyCampMember(profileId);

        // RiseMember 新增记录
        String memberId = generateMemberId();
        RiseClassMember classMember = new RiseClassMember();
        classMember.setClassId(ConfigUtils.getMonthlyCampClassId());
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
        Date endDate = ConfigUtils.getMonthlyCampEndDate();
        riseMember.setExpireDate(endDate);
        riseMemberDao.insert(riseMember);

        // 送优惠券
        Coupon coupon = new Coupon();
        coupon.setOpenid(profile.getOpenid());
        coupon.setProfileId(profileId);
        coupon.setAmount(MONTHLY_CAMP_COUPON);
        coupon.setUsed(0);
        coupon.setExpiredDate(DateUtils.afterYears(new Date(), 1));
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

        sendPurchaseMessage(profile, RiseMember.MONTHLY_CAMP, orderId);
        // 刷新相关状态
        refreshStatus(quanwaiOrderDao.loadOrder(orderId), orderId);
    }

    @Override
    public MonthlyCampOrder getMonthlyCampOrder(String orderId) {
        return monthlyCampOrderDao.loadCampOrder(orderId);
    }

    /**
     * 生成 memberId，格式 201701001 年 + 月 + 自然顺序
     */
    private String generateMemberId() {
        StringBuilder targetMemberId = new StringBuilder();

        String prefix = ConfigUtils.getMonthlyCampClassId();
        String key = "customer:trainCamp:" + prefix;
        redisUtil.lock("lock:memberId", (lock) -> {
            // TODO 有效期 60 天，期间 redis 绝对不能重启！！！
            String memberId = redisUtil.get(key);
            String sequence;
            if (StringUtils.isEmpty(memberId)) {
                sequence = "001";
            } else {
                sequence = String.format("%03d", Integer.parseInt(memberId) + 1);
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
        switch (memberType.getId()) {
            case 3: {
                //精英会员一年
                expireDate = DateUtils.afterNatureMonths(new Date(), 12);
                profileDao.becomeRiseEliteMember(openId);
                // 购买精英会员送 12 张线下工作坊券
                presentOfflineCoupons(riseOrder.getProfileId());
                break;
            }
            case 4: {
                //精英会员半年啊
                expireDate = DateUtils.afterNatureMonths(new Date(), 6);
                profileDao.becomeRiseEliteMember(openId);
                break;
            }
            default:
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
        for (ImprovementPlan plan : plans) {
            if (!plan.getRiseMember()) {
                // 不是会员的计划，设置一下
                plan.setCloseDate(DateUtils.afterDays(new Date(), PROBLEM_MAX_LENGTH));
                if (plan.getStatus().equals(1) && (memberType.getId().equals(3) || memberType.getId().equals(4))) {
                    // 给精英版正在进行的planid+1个求点评次数
                    improvementPlanDao.becomeRiseEliteMember(plan);
                } else {
                    // 非精英版或者不是正在进行的，不加点评次数
                    improvementPlanDao.becomeRiseMember(plan);
                }
            }
        }
        Profile profile = profileDao.queryByOpenId(openId);
        // 发送模板消息
        sendPurchaseMessage(profile, memberType.getId(), orderId);
    }

    private void sendPurchaseMessage(Profile profile, Integer memberTypeId, String orderId) {
        Assert.notNull(profile, "openid不能为空");
        logger.info("发送欢迎消息给付费用户{}", profile.getOpenid());

        switch (memberTypeId) {
            case RiseMember.ELITE: {
                // 发送消息给一年精英版的用户
                customerMessageService.sendCustomerMessage(profile.getOpenid(), ConfigUtils.getValue("risemember.elite.pay.send.image"), Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
                messageService.sendMessage("圈外每月小课训练营，戳此入群", Objects.toString(profile.getId()), MessageService.SYSTEM_MESSAGE, ConfigUtils.getValue("risemember.pay.send.system.url"));
                break;
            }
            case RiseMember.MONTHLY_CAMP: {
                // 发送消息给小课训练营购买用户
                customerMessageService.sendCustomerMessage(profile.getOpenid(), ConfigUtils.getValue("risemember.elite.pay.send.image"), Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
                messageService.sendMessage("圈外每月小课训练营，戳此入群", Objects.toString(profile.getId()), MessageService.SYSTEM_MESSAGE, ConfigUtils.getValue("risemember.pay.send.system.url"));
                break;
            }
            default: {
                messageService.sendAlarm("报名模块出错", "报名后发送消息", "中", "订单id:" + orderId + "\nprofileId:" + profile.getId(), "会员类型异常");
            }
        }
    }

    private void sendProfessionalWelcomeMsg(Profile profile, MemberType memberType, RiseMember riseMember) {
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(profile.getOpenid());
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setTemplate_id(ConfigUtils.productPaidMsg());
        String first = "Hi，" + profile.getNickname() + "，欢迎使用【圈外同学】正式版！\n\n";
        first += "所有圈外小课已为你开放，快来学习哦！\n";
        data.put("first", new TemplateMessage.Keyword(first));
        data.put("keyword1", new TemplateMessage.Keyword(memberType.getName()));
        data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
        data.put("keyword3", new TemplateMessage.Keyword(DateUtils.parseDateToString(DateUtils.beforeDays(riseMember.getExpireDate(), 1))));
        data.put("remark", new TemplateMessage.Keyword("\n想和更多优质小伙伴一起玩耍？点击详情，加入你所在地的分舵，玩转【圈外同学】吧～"));
        templateMessage.setUrl(ConfigUtils.domainName() + "/static/quanwai/wx/group");
        templateMessageService.sendMessage(templateMessage);
    }

    private void sendEliteWelcomeMsg(String openid, MemberType memberType, RiseMember riseMember) {
        String key = ConfigUtils.productPaidMsg();
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(openid);
        templateMessage.setTemplate_id(key);
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);

        data.put("first", new TemplateMessage.Keyword("Hi，" + profileDao.queryByOpenId(openid).getNickname() + "，欢迎进入【圈外同学】的学习旅程！\n"
                + "现在还有最后一步——加入精英社群，大部分学习交流和服务通知都要在社群里完成，请务必入坑，找到你的精英小伙伴们。\n"
                + "方式：点击详情，添加小Q，获得入群邀请～\n"));
        data.put("keyword1", new TemplateMessage.Keyword(memberType.getName()));
        data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
        data.put("keyword3", new TemplateMessage.Keyword(DateUtils.parseDateToString(DateUtils.beforeDays(riseMember.getExpireDate(), 1))));
//        data.put("remark", new TemplateMessage.Keyword("\n想扩展人脉，和精英RISER相互勾搭？点击详情，添加小Q，获得入群邀请哦～"));
        templateMessage.setUrl("https://shimo.im/doc/pwp5qEcft2sKABtL");

        templateMessageService.sendMessage(templateMessage);
    }

    private Date getCloseDate(Integer classId, Integer courseId) {
        Date closeDate = null;
        //长课程关闭时间=课程结束时间+7,短课程关闭时间=今天+课程长度+7,试听课程关闭时间为2999
        if (getCachedCourse(courseId).getType() == Course.LONG_COURSE) {
            Date closeTime = getCachedClass(classId).getCloseTime();
            closeDate = DateUtils.afterDays(closeTime, CourseStudyService.EXTRA_OPEN_DAYS);
        } else if (getCachedCourse(courseId).getType() == Course.SHORT_COURSE) {
            int length = getCachedCourse(courseId).getLength();
            closeDate = DateUtils.afterDays(new Date(), length + CourseStudyService.EXTRA_OPEN_DAYS);
        } else if (getCachedCourse(courseId).getType() == Course.AUDITION_COURSE) {
            closeDate = DateUtils.afterDays(new Date(), CourseStudyService.AUDITION_OPEN_DAYS);
        }
        return closeDate;
    }

    @Override
    public void giveupSignup(String orderId) {
        CourseOrder courseOrder = courseOrderDao.loadOrder(orderId);
        ClassMember classMember = classMemberDao.getClassMember(courseOrder.getClassId(), courseOrder.getProfileId());
        //未付款时退还名额
        if (classMember == null) {
            classMemberCountRepo.quitClass(courseOrder.getProfileId(), courseOrder.getCourseId(),
                    courseOrder.getClassId());
        }
        //关闭订单
        courseOrderDao.closeOrder(orderId);
        quanwaiOrderDao.closeOrder(orderId);
    }

    @Override
    public void giveupRiseCourseSignup(String orderId) {
        //关闭订单
        riseCourseOrderDao.closeOrder(orderId);
        quanwaiOrderDao.closeOrder(orderId);
    }

    @Override
    public void giveupRiseSignup(String orderId) {
        RiseOrder riseOrder = riseOrderDao.loadOrder(orderId);
        Profile profile = profileDao.queryByOpenId(riseOrder.getOpenid());
        Integer count = riseOrderDao.userNotCloseOrder(riseOrder.getProfileId());
        if (profile.getRiseMember() == Constants.RISE_MEMBER.FREE && count == 1) {
            // 未成功报名，并且是最后一个单子,退还名额
            riseMemberCountRepo.quitSignup(riseOrder.getProfileId(), riseOrder.getMemberType());
        }

        //关闭订单
        riseOrderDao.closeOrder(orderId);
        quanwaiOrderDao.closeOrder(orderId);
    }

    private void sendWelcomeMsg(Integer courseId, String openid, Integer classId, String memberId) {
        logger.info("发送欢迎消息给{}", openid);
        String key = ConfigUtils.signupSuccessMsgKey();
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(openid);
        templateMessage.setTemplate_id(key);
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);

        QuanwaiClass quanwaiClass = classDao.load(QuanwaiClass.class, classId);
        CourseIntroduction course = courseIntroductionDao.load(CourseIntroduction.class, courseId);

        if (course.getType() == Course.LONG_COURSE) {
            data.put("first", new TemplateMessage.Keyword("你已成功报名圈外训练营，还差最后一步--加群。\n"));
            data.put("keyword1", new TemplateMessage.Keyword(course.getCourseName()));
            data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToStringByCommon(quanwaiClass.getOpenTime()) + "-" + DateUtils.parseDateToStringByCommon(quanwaiClass.getCloseTime())));
            String remark = "\n到期后自动关闭\n\n你的学号是" + memberId + "\n只有加入微信群，才能顺利开始学习，点击查看二维码，长按识别即可入群。\n点开我->->->->->->";
            data.put("remark", new TemplateMessage.Keyword(remark));
            templateMessage.setUrl(quanwaiClass.getWeixinGroup());
        } else if (course.getType() == Course.SHORT_COURSE) {
            data.put("first", new TemplateMessage.Keyword("你已成功报名圈外训练营。"));
            data.put("keyword1", new TemplateMessage.Keyword(course.getCourseName()));
            data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToStringByCommon(new Date()) + "-" +
                    DateUtils.parseDateToStringByCommon(DateUtils.afterDays(new Date(), course.getLength() + 6))));
            if (quanwaiClass.getBroadcastUrl() != null) {
                String remark = "\n这里集合了关于本课程的共性问题，点击即可查看历史答疑汇总";
                data.put("remark", new TemplateMessage.Keyword(remark));
                templateMessage.setUrl(quanwaiClass.getBroadcastUrl());
            }

        } else if (course.getType() == Course.AUDITION_COURSE) {
            data.put("keyword1", new TemplateMessage.Keyword("【免费体验】 " + course.getCourseName()));
            data.put("keyword2", new TemplateMessage.Keyword("7天"));
            data.put("remark", new TemplateMessage.Keyword("\n试听截取正式课程的第一小节，完成试听后可以查看正式课程介绍"));
//            templateMessage.setUrl(quanwaiClass.getBroadcastUrl());
        }

        templateMessageService.sendMessage(templateMessage);
        //发送直播消息
//        if(course.getType()==Course.SHORT_COURSE && quanwaiClass.getBroadcastUrl()!=null){
//            templateMessage = new TemplateMessage();
//            templateMessage.setTouser(openid);
//            templateMessage.setTemplate_id(ConfigUtils.qaMsgKey());
//            data = Maps.newHashMap();
//            data.put("first", new TemplateMessage.Keyword("这里集合了关于本课程的共性问题，点击即可查看历史答疑汇总\n"));
//            data.put("keyword1", new TemplateMessage.Keyword("可随时回放"));
//            data.put("keyword2", new TemplateMessage.Keyword(course.getCourseName()));
//            data.put("keyword3", new TemplateMessage.Keyword("1.5小时"));
//            data.put("keyword4", new TemplateMessage.Keyword("14天"));
//            data.put("remark", new TemplateMessage.Keyword("\n如有新问题，在课程QQ群中和同学讨论哦（群号见上条报名成功消息）"));
//            templateMessage.setUrl(quanwaiClass.getBroadcastUrl());
//            templateMessage.setData(data);
//            templateMessageService.sendMessage(templateMessage);
//        }
    }

    @Override
    public void reloadClass() {
        init();
        //初始化班级剩余人数
        classMemberCountRepo.initClass();
        //初始化白名单和优惠券
        costRepo.reloadCache();
    }

    @Override
    public Map<Integer, Integer> getRemindingCount() {
        return classMemberCountRepo.getRemainingCount();
    }

    @Override
    public Integer getRiseRemindingCount() {
        return riseMemberCountRepo.getRemindingCount();
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
            coupons.forEach(item -> {
                item.setExpired(DateUtils.parseDateToStringByCommon(item.getExpiredDate()));
            });
            return coupons;
        }
        return Lists.newArrayList();
    }

    @Override
    public List<MemberType> getMemberTypesPayInfo() {
        List<MemberType> memberTypes = riseMemberTypeRepo.memberTypes();
        memberTypes.forEach(item -> {
            item.setStartTime(DateUtils.parseDateToStringByCommon(new Date()));
            item.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(DateUtils.afterNatureMonths(new Date(), item.getOpenMonth()), 1)));
        });
        return memberTypes;
    }

    @Override
    public Double calculateCoupon(Integer memberTypeId, Integer couponId) {
        Coupon coupon = costRepo.getCoupon(couponId);
        Double amount = coupon.getAmount();
        MemberType memberType = riseMemberTypeRepo.memberType(memberTypeId);
        if (memberType.getFee() >= amount) {
            return CommonUtils.substract(memberType.getFee(), amount);
        } else {
            return 0D;
        }
    }

    @Override
    public Double calculateCourseCoupon(Integer problemId, Integer profileId, Integer couponId) {
        Coupon coupon = costRepo.getCoupon(couponId);
        Double amount = coupon.getAmount();
        Double fee = this.getCoursePrice(profileId, problemId);
        if (fee >= amount) {
            return CommonUtils.substract(fee, amount);
        } else {
            return 0D;
        }
    }

    @Override
    public Double calculateCampCoupon(Integer profileId, Integer couponId) {
        List<Coupon> coupons = couponDao.loadCoupons(profileId);
        Coupon usingCoupon = coupons.stream().map(coupon -> {
            if(coupon.getId() == couponId) {
                return coupon;
            } else {
                return null;
            }
        }).findAny().get();
        Assert.notNull(usingCoupon, "正在使用的优惠券不能为空");
        Double fee = ConfigUtils.getMonthlyCampFee();
        if(fee >= usingCoupon.getAmount()) {
            return CommonUtils.substract(fee, usingCoupon.getAmount());
        } else {
            // 最少不能低于一分钱
            return 0.01D;
        }
    }

    @Override
    public RiseMember currentRiseMember(Integer profileId) {
        RiseMember riseMember = riseMemberDao.validRiseMember(profileId);
        if (riseMember != null) {
            riseMember.setStartTime(DateUtils.parseDateToStringByCommon(riseMember.getAddTime()));
            riseMember.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(riseMember.getExpireDate(), 1)));
        }
        return riseMember;
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
        }
        // 不管是否使用优惠券，此时都刷新优惠券信息
        costRepo.updateCoupon(Coupon.USED, orderId);
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

    private QuanwaiOrder createQuanwaiOrder(String openId, String orderId, Double fee, Double discount, String goodsId, String goodsName, String goodsType) {
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

    private void presentOfflineCoupons(Integer profileId) {
        Profile profile = accountService.getProfile(profileId);
        Coupon coupon = new Coupon();
        coupon.setOpenid(profile.getOpenid());
        coupon.setProfileId(profileId);
        coupon.setAmount(RISEMEMBER_OFFLINE_COUPON);
        coupon.setUsed(0);
        coupon.setExpiredDate(DateUtils.afterMonths(new Date(), 13));

        for (int i = 1; i < 13; i++) {
            Coupon tempCoupon = coupon;
            tempCoupon.setDescription(i + "月线下工作坊券");
            couponDao.insert(tempCoupon);
        }
    }
}
