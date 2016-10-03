package com.iquanwai.confucius.biz.domain.course.signup;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.zxing.WriterException;
import com.iquanwai.confucius.biz.dao.course.*;
import com.iquanwai.confucius.biz.dao.wx.CourseOrderDao;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.confucius.biz.po.*;
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
    private CourseDao courseDao;
    @Autowired
    private CourseIntroductionDao courseIntroductionDao;
    @Autowired
    private CourseOrderDao courseOrderDao;
    @Autowired
    private ClassMemberCountRepo classMemberCountRepo;
    @Autowired
    private ClassMemberDao classMemberDao;
    @Autowired
    private CourseFreeListDao courseFreeListDao;
    @Autowired
    private CouponDao couponDao;
    @Autowired
    private TemplateMessageService templateMessageService;
    //课程白名单
    private Map<Integer, List<String>> whiteList = Maps.newHashMap();

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 支付二维码的高度
     * */
    private static int QRCODE_HEIGHT = 200;
    /**
     * 支付二维码的宽度
     * */
    private static int QRCODE_WIDTH = 200;

    /**
     * 每个班级的当前学号
     * */
    private Map<Integer, Integer> memberCount = Maps.newConcurrentMap();

    private Map<Integer, SoftReference<QuanwaiClass>> classMap = Maps.newHashMap();
    private Map<Integer, SoftReference<CourseIntroduction>> courseMap = Maps.newHashMap();

    public Pair<Integer, Integer> signupCheck(String openid, Integer courseId) {
        if(!ConfigUtils.pressTestSwitch()) {
            if (classMemberCountRepo.isEntry(openid)) {
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
        double discount = discount(openid, course.getFee());
        courseOrder.setDiscount(discount);
        courseOrder.setPrice(course.getFee()-discount);
        courseOrder.setStatus(0); //待支付
        String orderId = CommonUtils.randomString(16);
        courseOrder.setOrderId(orderId);
        courseOrder.setCourseName(course.getCourseName());

        courseOrderDao.insert(courseOrder);

        return orderId;
    }

    public ClassMember classMember(String openid, Integer classId) {
        return classMemberDao.getClassMember(classId, openid);
    }

    public String payQRCode(String productId) {
        String payUrl = payUrl(productId);
        String path = "/data/static/images/qrcode/"+productId+".jpg";
        try {
            //生成二维码base64编码
            Image image = QRCodeUtils.genQRCode(payUrl, QRCODE_WIDTH, QRCODE_HEIGHT);

            QRCodeUtils.image2FS(image, path);
        } catch (WriterException e) {
            logger.error("二维码生成失败", e);
        }
        return path;
    }

    //折扣计算
    private double discount(String openid, Double price) {
        List<Coupon> coupons = couponDao.getCoupon(openid);
        List<Integer> usedCoupon = Lists.newArrayList();
        double remain = price;
        for(Coupon coupon:coupons){
            double amount = coupon.getAmount();
            if(remain>=amount){
                remain = remain-amount;
                if(remain==0.0){
                    break;
                }
            }else{
                double newAmount = amount-remain;
                Coupon newCoupon = new Coupon();
                newCoupon.setAmount(newAmount);
                newCoupon.setExpiredDate(defaultExpiredDate());
                newCoupon.setOpenid(openid);
                newCoupon.setUsed(0);
                couponDao.insert(coupon);
                break;
            }
            usedCoupon.add(coupon.getId());
        }
        couponDao.updateCoupon(usedCoupon, 2);
        return price-remain;
    }

    private Date defaultExpiredDate() {
        return DateUtils.afterYears(new Date(), 100);
    }

    public QuanwaiClass getCachedClass(Integer classId) {
        if(classMap.get(classId)==null || classMap.get(classId).get()==null){
            QuanwaiClass quanwaiClass = classDao.load(QuanwaiClass.class, classId);
            if(quanwaiClass!=null){
                classMap.put(classId, new SoftReference<QuanwaiClass>(quanwaiClass));
            }
        }
        return classMap.get(classId).get();
    }

    public CourseIntroduction getCachedCourse(Integer courseId) {
        if(courseMap.get(courseId)==null || courseMap.get(courseId).get()==null){
            CourseIntroduction course = courseIntroductionDao.getByCourseId(courseId);
            if(course!=null){
                courseMap.put(courseId, new SoftReference<CourseIntroduction>(course));
            }
        }
        return courseMap.get(courseId).get();
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
        String memberId = memberId(courseId, classId);
        classMember.setMemberId(memberId);
        classMemberDao.entry(classMember);
        //发送录取消息
        sendWelcomeMsg(courseId, openid, classId);
        return memberId;
    }

    public boolean isWhite(Integer courseId, String openid) {
        List<String> classWhiteList = whiteList.get(courseId);
        if(classWhiteList==null||!classWhiteList.contains(openid)){
            return false;
        }
        return true;
    }

    public void giveupSignup(String openid) {
        classMemberCountRepo.quitClass(openid);
    }

    public void sendWelcomeMsg(Integer courseId, String openid, Integer classId) {
        String key = ConfigUtils.signupSuccessMsgKey();
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(openid);
        templateMessage.setTemplate_id(key);
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);

        ClassMember classMember = classMemberDao.getClassMember(classId, openid);
        QuanwaiClass quanwaiClass = classDao.load(QuanwaiClass.class, classId);
        Course course = courseDao.load(Course.class, courseId);


        data.put("first",new TemplateMessage.Keyword("你已成功报名圈外训练营"));
        data.put("keyword1",new TemplateMessage.Keyword(course.getName()));
        data.put("keyword2",new TemplateMessage.Keyword(quanwaiClass.getOpenTime()+"-"+quanwaiClass.getCloseTime()));

        String remark = "你的学号是"+classMember.getMemberId()+"课程开始前先加入训练微信群，去认识一下你的同伴、助教和圈圈吧，点击查看群二维码。";
        data.put("remark",new TemplateMessage.Keyword(remark));
        templateMessage.setUrl(quanwaiClass.getWeixinGroup());
        templateMessageService.sendMessage(templateMessage);
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
        String product_id = productId;

        Map<String, String> map = Maps.newHashMap();
        map.put("nonce_str", nonce_str);
        map.put("time_stamp", time_stamp);
        map.put("appid", appid);
        map.put("mch_id", mch_id);
        map.put("product_id", product_id);
        //生成签名
        String sign = CommonUtils.sign(map);
        map.put("sign",sign);

        return CommonUtils.urlReplace(PAY_URL, map);
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

    @PostConstruct
    public void initWhiteList(){
        List<CourseFreeList> courseFreeLists = courseFreeListDao.loadAll(CourseFreeList.class);
        for(CourseFreeList freeList:courseFreeLists){
            List<String> openids = whiteList.get(freeList.getCourseId());
            if(openids==null){
                openids = Lists.newArrayList();
                whiteList.put(freeList.getCourseId(), openids);
            }
            openids.add(freeList.getOpenid());
        }
        logger.info("init white list complete");
    }

}
