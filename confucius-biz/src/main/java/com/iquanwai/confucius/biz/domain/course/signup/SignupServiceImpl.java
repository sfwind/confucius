package com.iquanwai.confucius.biz.domain.course.signup;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.zxing.WriterException;
import com.iquanwai.confucius.biz.dao.course.ClassDao;
import com.iquanwai.confucius.biz.dao.course.ClassMemberDao;
import com.iquanwai.confucius.biz.dao.course.CourseIntroductionDao;
import com.iquanwai.confucius.biz.dao.wx.CourseOrderDao;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.confucius.biz.po.ClassMember;
import com.iquanwai.confucius.biz.po.CourseIntroduction;
import com.iquanwai.confucius.biz.po.CourseOrder;
import com.iquanwai.confucius.biz.po.QuanwaiClass;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.QRCodeUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private ClassMemberCountRepo classMemberCountRepo;
    @Autowired
    private ClassMemberDao classMemberDao;
    @Autowired
    private CostRepo costRepo;
    @Autowired
    private TemplateMessageService templateMessageService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 待付费名单(openid+courseId)
     * */
    private List<String> payList = Lists.newArrayList();

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
        List<CourseOrder> courseOrders = courseOrderDao.loadClassOrder(openClass);
        //清空缓存
        payList.clear();
        classMap.clear();
        courseMap.clear();
        courseOrders.stream().filter(courseOrder -> !payList.contains(courseOrder.getOpenid() + courseOrder.getCourseId())).forEach(courseOrder -> {
            payList.add(courseOrder.getOpenid() + courseOrder.getCourseId());
        });

        logger.info("init under payment map complete");
    }


    public Pair<Integer, Integer> signupCheck(String openid, Integer courseId) {
        if(!ConfigUtils.pressTestSwitch()) {
            if (classMemberCountRepo.isEntry(openid, courseId) && !payList.contains(openid+courseId)) {
                return new ImmutablePair(-3, 0);
            }
        }

        return classMemberCountRepo.prepareSignup(openid, courseId);
    }

    public String signup(String openid, Integer courseId, Integer classId) {
        //生成订单
        CourseOrder courseOrder = new CourseOrder();
        courseOrder.setClassId(classId);
        courseOrder.setCourseId(courseId);
        courseOrder.setCreateTime(new Date());
        courseOrder.setOpenid(openid);

        CourseIntroduction course = getCachedCourse(courseId);
        if(course == null){
            logger.error("courseId {} is not existed", courseId);
            return null;
        }
        double discount = 0.0;
        //计算优惠
        if(costRepo.hasCoupon(openid)) {
            discount = costRepo.discount(course.getFee(), openid);
        }
        courseOrder.setDiscount(discount);
        courseOrder.setPrice(course.getFee()-discount);
        courseOrder.setStatus(0); //待支付
        String orderId = CommonUtils.randomString(16);
        courseOrder.setOrderId(orderId);
        courseOrder.setCourseName(course.getCourseName());

        courseOrderDao.insert(courseOrder);
        //加入待付款列表
        if(!payList.contains(openid+course.getCourseId())) {
            payList.add(openid + course.getCourseId());
        }
        return orderId;
    }

    public ClassMember classMember(String openid, Integer classId) {
        return classMemberDao.getClassMember(classId, openid);
    }

    public String payQRCode(String productId) {
        String payUrl = payUrl(productId);
        String path = "/data/static/images/qrcode/"+productId+".jpg";
        String picUrl = ConfigUtils.domainName()+"/images/qrcode/"+productId+".jpg";
        try {
            //生成二维码base64编码
            Image image = QRCodeUtils.genQRCode(payUrl, QRCODE_WIDTH, QRCODE_HEIGHT);

            QRCodeUtils.image2FS(image, path);
        } catch (WriterException e) {
            logger.error("二维码生成失败", e);
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

    public CourseOrder getCourseOrder(String orderId) {
        return courseOrderDao.loadOrder(orderId);
    }

    public String entry(Integer courseId, Integer classId, String openid) {
        ClassMember classMember = classMemberDao.getClassMember(classId, openid);
        if(classMember!=null){
            return classMember.getMemberId();
        }
        classMember = new ClassMember();
        classMember.setClassId(classId);
        classMember.setOpenId(openid);
        classMember.setCourseId(courseId);
        String memberId = memberId(courseId, classId);
        classMember.setMemberId(memberId);
        classMemberDao.entry(classMember);
        //从待付款列表中去除
        payList.remove(openid+courseId);
        //发送录取消息
        sendWelcomeMsg(courseId, openid, classId);
        return memberId;
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

    public void giveupSignup(String openid, String orderId) {
        courseOrderDao.closeOrder(orderId);

        CourseOrder courseOrder = courseOrderDao.loadOrder(orderId);
        payList.remove(openid+courseOrder.getCourseId());
        ClassMember classMember = classMemberDao.getClassMember(courseOrder.getClassId(), openid);
        //已经报名成功的学员不需要退班
        if(classMember==null) {
            classMemberCountRepo.quitClass(openid, courseOrder.getCourseId());
        }
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

        data.put("first",new TemplateMessage.Keyword("你已成功报名圈外训练营，还差最后一步--加群。"));
        data.put("keyword1",new TemplateMessage.Keyword(course.getCourseName()));
        data.put("keyword2",new TemplateMessage.Keyword(quanwaiClass.getOpenTime()+"-"+quanwaiClass.getCloseTime()));
        String remark = "你的学号是"+classMember.getMemberId()+"\n只有加入微信群，才能顺利开始学习，点击查看二维码，长按识别即可入群。\n点开我->->->->->->";
        data.put("remark",new TemplateMessage.Keyword(remark));
        templateMessage.setUrl(quanwaiClass.getWeixinGroup());

        templateMessageService.sendMessage(templateMessage);
    }

    public void reloadClass() {
        init();
        //初始化班级剩余人数
        classMemberCountRepo.initClass();
        //初始化白名单和优惠券
        costRepo.reloadCache();
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

}
