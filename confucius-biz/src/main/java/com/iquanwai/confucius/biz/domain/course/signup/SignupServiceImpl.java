package com.iquanwai.confucius.biz.domain.course.signup;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.course.ClassDao;
import com.iquanwai.confucius.biz.dao.course.ClassMemberDao;
import com.iquanwai.confucius.biz.dao.course.CourseIntroductionDao;
import com.iquanwai.confucius.biz.dao.course.CourseOrderDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseOrderDao;
import com.iquanwai.confucius.biz.dao.wx.QuanwaiOrderDao;
import com.iquanwai.confucius.biz.domain.course.progress.CourseStudyService;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.po.fragmentation.RiseOrder;
import com.iquanwai.confucius.biz.po.systematism.ClassMember;
import com.iquanwai.confucius.biz.po.systematism.Course;
import com.iquanwai.confucius.biz.po.systematism.CourseIntroduction;
import com.iquanwai.confucius.biz.po.systematism.CourseOrder;
import com.iquanwai.confucius.biz.po.systematism.QuanwaiClass;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.QRCodeUtils;
import lombok.Data;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.lang.ref.SoftReference;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/9/10.
 */
@Service
public class SignupServiceImpl implements SignupService {
    @Autowired
    private ClassDao classDao;
    @Autowired
    private CourseIntroductionDao courseIntroductionDao;
    @Autowired
    private CourseOrderDao courseOrderDao;
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

    int PROBLEM_MAX_LENGTH = 30; //专题最长开放时间

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 付费名单(openid+courseId)
     * */
    private List<Payment> payList = Lists.newArrayList();

    /**
     * 支付二维码的高度
     * */
    private final static int QRCODE_HEIGHT = 200;
    /**
     * 支付二维码的宽度
     * */
    private final static int QRCODE_WIDTH = 200;

    /**
     * 每个班级的当前学号
     * */
    private Map<Integer, Integer> memberCount = Maps.newConcurrentMap();

    private Map<Integer, SoftReference<QuanwaiClass>> classMap = Maps.newHashMap();
    private Map<Integer, CourseIntroduction> courseMap = Maps.newHashMap();

    @PostConstruct
    /**
     * 初始化缓存
     * */
    public void init(){
        //统计待付款的人数
        List<QuanwaiClass> quanwaiClassList = classDao.openClass();
        List<Integer> openClass = Lists.newArrayList(); // 开放报名的班级id
        for (QuanwaiClass quanwaiClass : quanwaiClassList) {
            Integer classId = quanwaiClass.getId();
            openClass.add(classId);
        }
        List<ClassMember> members = classMemberDao.loadActiveMembers();
        //
        //清空缓存
        payList.clear();
        classMap.clear();
        courseMap.clear();
        members.stream().filter(member -> !payList.contains(new Payment(member.getOpenId(),member.getCourseId())))
                .forEach(member -> payList.add(new Payment(member.getOpenId(), member.getCourseId())));

        logger.info("init under payment map complete");
    }


    public Pair<Integer, Integer> signupCheck(String openid, Integer courseId) {
//        if(!ConfigUtils.pressTestSwitch()) {
//            ClassMember classMember = classMemberDao.classMember(openid, courseId);
//            if(classMember!=null){
//                // 并且他正在进行的课程里有这门课
//                if (DateUtils.startDay(new Date()).before(classMember.getCloseDate())) {
//                    return new ImmutablePair(-3, 0);
//                }
//            }
//        }
        // 还没有正式进入班级
        return classMemberCountRepo.prepareSignup(openid, courseId);
    }

    @Override
    public Pair<Integer, String> riseMemberSignupCheck(String openId,Integer memberTypeId){
        return riseMemberCountRepo.prepareSignup(openId);
    }

    public QuanwaiOrder signup(String openid, Integer courseId, Integer classId) {
        //生成订单
        QuanwaiOrder quanwaiOrder = new QuanwaiOrder();
        quanwaiOrder.setCreateTime(new Date());
        quanwaiOrder.setOpenid(openid);
        //orderId 16位随机字符
        String orderId = CommonUtils.randomString(16);
        quanwaiOrder.setOrderId(orderId);
        CourseIntroduction course = getCachedCourse(courseId);
        if(course == null){
            logger.error("courseId {} is not existed", courseId);
            return null;
        }
        double discount = 0.0;
        //计算优惠
        if(costRepo.hasCoupon(openid)) {
            discount = costRepo.discount(course.getFee(), openid, orderId);
        }
        quanwaiOrder.setTotal(course.getFee());
        quanwaiOrder.setDiscount(discount);
        quanwaiOrder.setPrice(CommonUtils.substract(course.getFee(), discount));
        quanwaiOrder.setStatus(QuanwaiOrder.UNDER_PAY); //待支付
        quanwaiOrder.setGoodsId(courseId+"");
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
        courseOrder.setOrderId(orderId);
        courseOrderDao.insert(courseOrder);

        return quanwaiOrder;
    }

    @Override
    public QuanwaiOrder signupRiseMember(String openid, Integer memberTypeId){
        // 查询该openid是否是我们的用户
        Profile profile = profileDao.queryByOpenId(openid);
        MemberType memberType = riseMemberTypeRepo.memberType(memberTypeId);
        Assert.notNull(profile, "用户信息错误");
        Assert.notNull(memberType, "会员类型错误");
        // 创建订单
        QuanwaiOrder quanwaiOrder = new QuanwaiOrder();
        quanwaiOrder.setCreateTime(new Date());
        quanwaiOrder.setOpenid(openid);
        //orderId 16位随机字符
        String orderId = CommonUtils.randomString(16);
        quanwaiOrder.setOrderId(orderId);
        quanwaiOrder.setTotal(memberType.getFee());
        quanwaiOrder.setDiscount(0.0);
        quanwaiOrder.setPrice(memberType.getFee());
        quanwaiOrder.setStatus(QuanwaiOrder.UNDER_PAY);
        quanwaiOrder.setGoodsId(memberTypeId + "");
        quanwaiOrder.setGoodsName(memberType.getName());
        quanwaiOrder.setGoodsType(QuanwaiOrder.FRAGMENT_MEMBER);
        quanwaiOrderDao.insert(quanwaiOrder);

        // rise的报名数据
        RiseOrder riseOrder = new RiseOrder();
        riseOrder.setOpenid(openid);
        riseOrder.setEntry(false);
        riseOrder.setIsDel(false);
        riseOrder.setMemberType(memberTypeId);
        riseOrder.setOrderId(orderId);
        riseOrderDao.insert(riseOrder);
        return quanwaiOrder;
    }

    @Override
    public Pair<Integer,QuanwaiOrder> signupRiseMember(String openid, Integer memberTypeId,Integer couponId){
        // 查询该openid是否是我们的用户
        Profile profile = profileDao.queryByOpenId(openid);
        MemberType memberType = riseMemberTypeRepo.memberType(memberTypeId);
        Assert.notNull(profile, "用户信息错误");
        Assert.notNull(memberType, "会员类型错误");

        // 创建订单
        QuanwaiOrder quanwaiOrder = new QuanwaiOrder();
        quanwaiOrder.setCreateTime(new Date());
        quanwaiOrder.setOpenid(openid);
        //orderId 16位随机字符
        String orderId = CommonUtils.randomString(16);
        double discount = 0d;
        if (couponId != null) {
            // 计算优惠
            Coupon coupon = costRepo.getCoupon(couponId);
            if (coupon == null || coupon.getUsed() == Coupon.USED || coupon.getExpiredDate().before(new Date())) {
                // 优惠券无效
                return new MutablePair<>(-1, null);
            }
            discount = costRepo.discount(memberType.getFee(), openid, orderId, coupon);
        }



        quanwaiOrder.setOrderId(orderId);
        quanwaiOrder.setTotal(memberType.getFee());
        quanwaiOrder.setDiscount(discount);
        quanwaiOrder.setPrice(CommonUtils.substract(memberType.getFee(), discount));
        quanwaiOrder.setStatus(QuanwaiOrder.UNDER_PAY);
        quanwaiOrder.setGoodsId(memberTypeId + "");
        quanwaiOrder.setGoodsName(memberType.getName());
        quanwaiOrder.setGoodsType(QuanwaiOrder.FRAGMENT_MEMBER);
        quanwaiOrderDao.insert(quanwaiOrder);

        // rise的报名数据
        RiseOrder riseOrder = new RiseOrder();
        riseOrder.setOpenid(openid);
        riseOrder.setEntry(false);
        riseOrder.setIsDel(false);
        riseOrder.setMemberType(memberTypeId);
        riseOrder.setOrderId(orderId);
        riseOrderDao.insert(riseOrder);
        return new MutablePair<>(1, quanwaiOrder);
    }

    @Override
    public QuanwaiOrder signup(String openid, Integer courseId, Integer classId, String promoCode, Double discount) {
        //生成订单
        QuanwaiOrder quanwaiOrder = new QuanwaiOrder();
        quanwaiOrder.setCreateTime(new Date());
        quanwaiOrder.setOpenid(openid);
        //orderId 16位随机字符
        String orderId = CommonUtils.randomString(16);
        quanwaiOrder.setOrderId(orderId);
        CourseIntroduction course = getCachedCourse(courseId);
        if(course == null){
            logger.error("courseId {} is not existed", courseId);
            return null;
        }
        //计算优惠 该二维码不计算优惠券
//        if(costRepo.hasCoupon(openid)) {
//            discount = costRepo.discount(course.getFee(), openid, orderId);
//        }
        if(course.getFee().equals(discount) || course.getFee() < discount){
            discount = course.getFee();
        }
        quanwaiOrder.setTotal(course.getFee());
        quanwaiOrder.setDiscount(discount);
        quanwaiOrder.setPrice(CommonUtils.substract(course.getFee(), discount));
        quanwaiOrder.setStatus(QuanwaiOrder.UNDER_PAY); //待支付
        quanwaiOrder.setGoodsId(courseId+"");
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
        courseOrder.setOrderId(orderId);
        courseOrderDao.insert(courseOrder);

        return quanwaiOrder;
    }

    @Override
    public ClassMember classMember(String orderId) {
        CourseOrder courseOrder = courseOrderDao.loadOrder(orderId);
        if(courseOrder!=null){
            return classMemberDao.getClassMember(courseOrder.getClassId(), courseOrder.getOpenid());
        }
        return null;
    }

    public String payQRCode(String productId) {
        String payUrl = payUrl(productId);
        String path = "/data/static/images/qrcode/"+productId+".jpg";
        String picUrl = ConfigUtils.resourceDomainName()+"/images/qrcode/"+productId+".jpg";

        //生成二维码base64编码
        Image image = QRCodeUtils.genQRCode(payUrl, QRCODE_WIDTH, QRCODE_HEIGHT);
        if(image==null){
            logger.error("二维码生成失败");
        }else {
            QRCodeUtils.image2FS(image, path);
        }

        return picUrl;
    }

    public QuanwaiClass getCachedClass(Integer classId) {
        if(classMap.get(classId)==null || classMap.get(classId).get()==null){
            QuanwaiClass quanwaiClass = classDao.load(QuanwaiClass.class, classId);
            if(quanwaiClass!=null){
                classMap.put(classId, new SoftReference<>(quanwaiClass));
            }
        }
        return classMap.get(classId).get();
    }

    public CourseIntroduction getCachedCourse(Integer courseId) {
        if(courseMap.get(courseId)==null){
            CourseIntroduction course = courseIntroductionDao.getByCourseId(courseId);
            if(course!=null){
                courseMap.put(courseId, course);
            }
        }
        return courseMap.get(courseId);
    }

    public CourseOrder getOrder(String orderId) {
        return courseOrderDao.loadOrder(orderId);
    }

    public String entry(String orderId) {
        CourseOrder courseOrder = courseOrderDao.loadOrder(orderId);
        Integer classId = courseOrder.getClassId();
        Integer courseId = courseOrder.getCourseId();
        String openid = courseOrder.getOpenid();
        //体系化订单标记已报名
        courseOrderDao.entry(orderId);
        ClassMember classMember = classMemberDao.getClassMember(classId, openid);
        Date closeDate = getCloseDate(classId, courseId);
        if(classMember!=null){
            //已经毕业或者已经超过关闭时间,重置学员数据
            if(classMember.getGraduate() || classMember.getCloseDate().before(DateUtils.startDay(new Date()))){
                classMemberDao.reEntry(classMember.getId(), closeDate);
            }
            //发送录取消息
            sendWelcomeMsg(courseId, openid, classId);
            return classMember.getMemberId();
        }
        classMember = new ClassMember();
        classMember.setClassId(classId);
        classMember.setOpenId(openid);
        classMember.setCourseId(courseId);
        //只有长课程有学号
        String memberId = "";
        if(getCachedCourse(courseId).getType()== Course.LONG_COURSE) {
            memberId = memberId(courseId, classId);
            classMember.setMemberId(memberId);
        }
        //设置课程关闭时间
        classMember.setCloseDate(closeDate);

        classMemberDao.entry(classMember);
        //加入已付款列表
        if(!payList.contains(new Payment(openid, courseId))) {
            payList.add(new Payment(openid, courseId));
        }
        //发送录取消息
        sendWelcomeMsg(courseId, openid, classId);
        return memberId;
    }

    @Override
    public void riseMemberEntry(String orderId){
        RiseOrder riseOrder = riseOrderDao.loadOrder(orderId);
        riseOrderDao.entry(orderId);
        String openId = riseOrder.getOpenid();

        MemberType memberType = riseMemberTypeRepo.memberType(riseOrder.getMemberType());
        Date expireDate = null;
        switch (memberType.getId()) {
            case 1: {
                expireDate = DateUtils.afterMonths(new Date(), 6);
                profileDao.becomeRiseMember(openId);
                break;
            }
            case 2: {
                expireDate = DateUtils.afterYears(new Date(), 1);
                profileDao.becomeRiseMember(openId);
                break;
            }
            case 3: {
                expireDate = DateUtils.afterYears(new Date(), 1);
                profileDao.becomeRiseMember(openId);
                break;
            }
            default:
                logger.error("该会员ID异常{}", memberType);
                return;
        }
        // 添加会员表
        RiseMember riseMember = new RiseMember();
        riseMember.setOpenId(riseOrder.getOpenid());
        riseMember.setOrderId(riseOrder.getOrderId());
        riseMember.setMemberTypeId(memberType.getId());
        riseMember.setExpireDate(expireDate);
        riseMemberDao.insert(riseMember);

        // 所有计划设置为会员
        List<ImprovementPlan> plans = improvementPlanDao.loadUserPlans(riseOrder.getOpenid());
        for (ImprovementPlan plan : plans) {
            if(!plan.getRiseMember()){
                // 不是会员的计划，设置一下
                plan.setCloseDate(DateUtils.afterDays(new Date(), PROBLEM_MAX_LENGTH));
                improvementPlanDao.becomeRiseMember(plan);
            }
        }
    }

    private Date getCloseDate(Integer classId, Integer courseId) {
        Date closeDate = null;
        //长课程关闭时间=课程结束时间+7,短课程关闭时间=今天+课程长度+7,试听课程关闭时间为2999
        if(getCachedCourse(courseId).getType()== Course.LONG_COURSE) {
            Date closeTime = getCachedClass(classId).getCloseTime();
            closeDate = DateUtils.afterDays(closeTime, CourseStudyService.EXTRA_OPEN_DAYS);
        }else if(getCachedCourse(courseId).getType()==Course.SHORT_COURSE){
            int length = getCachedCourse(courseId).getLength();
            closeDate = DateUtils.afterDays(new Date(), length+CourseStudyService.EXTRA_OPEN_DAYS);
        } else if(getCachedCourse(courseId).getType() == Course.AUDITION_COURSE){
            closeDate = DateUtils.afterDays(new Date(), CourseStudyService.AUDITION_OPEN_DAYS);
        }
        return closeDate;
    }

    public boolean free(Integer courseId, String openid) {
        CourseIntroduction courseIntroduction = this.getCachedCourse(courseId);
        if(courseIntroduction==null){
            logger.error("courseId {} is invalid", courseId);
            return false;
        }

        return courseIntroduction.getFree() ||
                costRepo.isWhite(courseId, openid);
    }

    public void giveupSignup(String orderId) {
        CourseOrder courseOrder = courseOrderDao.loadOrder(orderId);
        ClassMember classMember = classMemberDao.getClassMember(courseOrder.getClassId(), courseOrder.getOpenid());
        //未付款时退还名额
        if(classMember==null) {
            classMemberCountRepo.quitClass(courseOrder.getOpenid(), courseOrder.getCourseId(),
                    courseOrder.getClassId());
        }
        //关闭订单
        courseOrderDao.closeOrder(orderId);
        quanwaiOrderDao.closeOrder(orderId);
    }

    @Override
    public void giveupRiseSignup(String orderId){
        RiseOrder riseOrder = riseOrderDao.loadOrder(orderId);
        Profile profile = profileDao.queryByOpenId(riseOrder.getOpenid());
        Integer count = riseOrderDao.userNotCloseOrder(riseOrder.getOpenid());
        if (!profile.getRiseMember() && count == 1) {
            // 未成功报名，并且是最后一个单子,退还名额
            riseMemberCountRepo.quitSignup(riseOrder.getOrderId(), riseOrder.getMemberType());
        }

        //关闭订单
        riseOrderDao.closeOrder(orderId);
        quanwaiOrderDao.closeOrder(orderId);
    }

    public void sendWelcomeMsg(Integer courseId, String openid, Integer classId) {
        logger.info("发送欢迎消息给{}", openid);
        String key = ConfigUtils.signupSuccessMsgKey();
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(openid);
        templateMessage.setTemplate_id(key);
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);

        ClassMember classMember = classMemberDao.getClassMember(classId, openid);
        QuanwaiClass quanwaiClass = classDao.load(QuanwaiClass.class, classId);
        CourseIntroduction course = courseIntroductionDao.load(CourseIntroduction.class, courseId);

        if(course.getType()==Course.LONG_COURSE) {
            data.put("first", new TemplateMessage.Keyword("你已成功报名圈外训练营，还差最后一步--加群。\n"));
            data.put("keyword1", new TemplateMessage.Keyword(course.getCourseName()));
            data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToStringByCommon(quanwaiClass.getOpenTime()) + "-" + DateUtils.parseDateToStringByCommon(quanwaiClass.getCloseTime())));
            String remark = "\n到期后自动关闭\n\n你的学号是" + classMember.getMemberId() + "\n只有加入微信群，才能顺利开始学习，点击查看二维码，长按识别即可入群。\n点开我->->->->->->";
            data.put("remark", new TemplateMessage.Keyword(remark));
            templateMessage.setUrl(quanwaiClass.getWeixinGroup());
        }else if(course.getType()==Course.SHORT_COURSE){
//            data.put("first", new TemplateMessage.Keyword("你已成功报名圈外训练营。"));
            data.put("keyword1", new TemplateMessage.Keyword(course.getCourseName()));
            data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToStringByCommon(new Date()) + "-" +
                    DateUtils.parseDateToStringByCommon(DateUtils.afterDays(new Date(), course.getLength()+6))));
            String remark = "\n到期后自动关闭\n\n想和更多求职的同伴一起学习？\n加入QQ群："+quanwaiClass.getQqGroupNo()
                    +"。点击查看群二维码。";
            data.put("remark", new TemplateMessage.Keyword(remark));
            templateMessage.setUrl(quanwaiClass.getQqGroup());
        } else if(course.getType()==Course.AUDITION_COURSE){
            data.put("keyword1", new TemplateMessage.Keyword("【免费体验】 "+course.getCourseName()));
            data.put("keyword2", new TemplateMessage.Keyword("7天"));
            data.put("remark", new TemplateMessage.Keyword("\n试听截取正式课程的第一小节，完成试听后可以查看正式课程介绍"));
//            templateMessage.setUrl(quanwaiClass.getBroadcastUrl());
        }

        templateMessageService.sendMessage(templateMessage);
        //发送直播消息
        if(course.getType()==Course.SHORT_COURSE && quanwaiClass.getBroadcastUrl()!=null){
            templateMessage = new TemplateMessage();
            templateMessage.setTouser(openid);
            templateMessage.setTemplate_id(ConfigUtils.qaMsgKey());
            data = Maps.newHashMap();
            data.put("first", new TemplateMessage.Keyword("这里集合了关于本课程的共性问题，点击即可查看历史答疑汇总\n"));
            data.put("keyword1", new TemplateMessage.Keyword("可随时回放"));
            data.put("keyword2", new TemplateMessage.Keyword(course.getCourseName()));
            data.put("keyword3", new TemplateMessage.Keyword("1.5小时"));
            data.put("keyword4", new TemplateMessage.Keyword("14天"));
            data.put("remark", new TemplateMessage.Keyword("\n如有新问题，在课程QQ群中和同学讨论哦（群号见上条报名成功消息）"));
            templateMessage.setUrl(quanwaiClass.getBroadcastUrl());
            templateMessage.setData(data);
            templateMessageService.sendMessage(templateMessage);
        }
    }

    public void reloadClass() {
        init();
        //初始化班级剩余人数
        classMemberCountRepo.initClass();
        //初始化白名单和优惠券
        costRepo.reloadCache();
    }

    @Override
    public CourseOrder getCourseOrder(String orderId) {
        return courseOrderDao.loadOrder(orderId);
    }

    @Override
    public List<QuanwaiOrder> getActiveOrders(String openId, Integer courseId) {
        return quanwaiOrderDao.loadActiveOrders(openId);
    }

    @Override
    public Map<Integer,Integer> getRemindingCount(){
        return classMemberCountRepo.getRemainingCount();
    }

    @Override
    public Integer getRiseRemindingCount(){
        return riseMemberCountRepo.getRemindingCount();
    }

    @Override
    public void updatePromoCode(String orderId, String promoCode){
        courseOrderDao.updatePromoCode(orderId,promoCode);
    }

    @Override
    public QuanwaiOrder getQuanwaiOrder(String orderId) {
        return quanwaiOrderDao.loadOrder(orderId);
    }

    @Override
    public RiseOrder getRiseOrder(String orderId){
        return riseOrderDao.loadOrder(orderId);
    }

    @Override
    public MemberType getMemberType(Integer memberType){
        return riseMemberTypeRepo.memberType(memberType);
    }

    @Override
    public List<Coupon> getCoupons(String openId){
        if (costRepo.hasCoupon(openId)) {
            List<Coupon> coupons = costRepo.getCoupons(openId);
            coupons.forEach(item->{
                item.setExpired(DateUtils.parseDateToStringByCommon(item.getExpiredDate()));
            });
            return coupons;
        }
        return Lists.newArrayList();
    }

    @Override
    public List<MemberType> getMemberTypesPayInfo() {
        List<MemberType> memberTypes = riseMemberTypeRepo.memberTypes();
        memberTypes.forEach(item->{
            item.setStartTime(DateUtils.parseDateToStringByCommon(new Date()));
            item.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(DateUtils.afterMonths(new Date(), item.getOpenMonth()), 1)));
        });
        return memberTypes;
    }

    @Override
    public Double calculateCoupon(Integer memberTypeId, Integer couponId){
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
    public RiseMember currentRiseMember(String openId){
        RiseMember riseMember = riseMemberDao.validRiseMember(openId);
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

    private String payUrl(String productId){
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
        map.put("sign",sign);

        return CommonUtils.placeholderReplace(PAY_URL, map);
    }

    public synchronized Integer getMemberNumber(Integer classId){
        if(memberCount.get(classId)==null){
            int number = classMemberDao.classMemberNumber(classId);
            memberCount.put(classId, number+1);
            return number+1;
        }

        int count = memberCount.get(classId)+1;
        memberCount.put(classId, count);
        return count;
    }

    @Data
    private static class Payment {
        private String openid;
        private Integer courseId;

        public Payment(String openid, Integer courseId){
            this.openid = openid;
            this.courseId = courseId;
        }
    }


}
