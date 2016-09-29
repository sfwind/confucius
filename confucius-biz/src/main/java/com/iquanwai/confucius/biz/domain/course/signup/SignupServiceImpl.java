package com.iquanwai.confucius.biz.domain.course.signup;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.zxing.WriterException;
import com.iquanwai.confucius.biz.dao.course.*;
import com.iquanwai.confucius.biz.dao.wx.CourseOrderDao;
import com.iquanwai.confucius.biz.po.*;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.QRCodeUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.lang.ref.SoftReference;
import java.util.Date;
import java.util.Iterator;
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
    private ClassMemberDao classMemberDao;
    @Autowired
    private CourseFreeListDao courseFreeListDao;
    @Autowired
    private CouponDao couponDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Object lock = new Object();
    private Object lock2 = new Object();

    /**
     * 支付二维码的高度
     * */
    private static int QRCODE_HEIGHT = 200;
    /**
     * 支付二维码的宽度
     * */
    private static int QRCODE_WIDTH = 200;

    /**
     * 每个班级的剩余人数
     * */
    private Map<Integer, Map<Integer,Integer>> remainingCount = Maps.newConcurrentMap();
    /**
     * 每个班级的当前学号
     * */
    private Map<Integer, Integer> memberCount = Maps.newConcurrentMap();

    private Map<Integer, SoftReference<QuanwaiClass>> classMap = Maps.newHashMap();
    private Map<Integer, SoftReference<CourseIntroduction>> courseMap = Maps.newHashMap();

    public Pair<Integer, Integer> signupCheck(String openid, Integer courseId) {

        //初始化课程报名人数
        if(remainingCount.get(courseId)==null){
            synchronized (lock){
                if(remainingCount.get(courseId)==null){
                    List<QuanwaiClass> quanwaiClass = classDao.openClass(courseId);
                    //TODO:没有班级需要报警
                    if(CollectionUtils.isEmpty(quanwaiClass)){
                        logger.error("course {} has no open class", courseId);
                        return new ImmutablePair(-2, 0);
                    }else{
                        Map<Integer,Integer> clazzNumber = Maps.newConcurrentMap();
                        for(QuanwaiClass clazz:quanwaiClass){
                            //TODO:班级人数设置问题,需要报警
                            if(clazz.getLimit()==null || clazz.getLimit()<=0){
                                logger.error("class {} 's limit is wrong", clazz.getId());
                                return new ImmutablePair(-3, clazz.getId());
                            }
                            Integer count = courseOrderDao.paidCount(clazz.getId());
                            if(count>=0){
                                int remaining = clazz.getLimit()-count>0?clazz.getLimit()-count:0;
                                clazzNumber.put(clazz.getId(), remaining);
                            }
                            classMap.put(clazz.getId(), new SoftReference<QuanwaiClass>(clazz));
                        }
                        remainingCount.put(courseId, clazzNumber);
                    }
                }
            }
        }
        //计算剩余人数
        synchronized (lock2) {
            Map<Integer, Integer> remaining = remainingCount.get(courseId);
            int remain = 0;
            boolean isEntry = false; //是否已经进入某班
            Integer classId = 0;
            for(Iterator<Map.Entry<Integer,Integer>> it =remaining.entrySet().iterator(); it.hasNext();){
                Map.Entry<Integer,Integer> entry = it.next();
                int remainingNumber = entry.getValue();
                if(remainingNumber==0){
                    continue;
                }else{
                    if(!isEntry) {
                        //人数-1，记录班级id，标记分配进入某班
                        remainingNumber--;
                        classId = entry.getKey();
                        entry.setValue(remainingNumber);
                        isEntry = true;
                    }
                }
                remain = remain+remainingNumber;
            }
            if(isEntry) {
                return new ImmutablePair<Integer, Integer>(remain, classId);
            }
        }
        return new ImmutablePair(-1, 0);
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
        if(classMemberDao.isEntry(classId, openid)){
            return null;
        }
        ClassMember classMember = new ClassMember();
        classMember.setClassId(classId);
        classMember.setOpenId(openid);
        String memberId = memberId(courseId, classId);
        classMember.setMemberId(memberId);
        classMemberDao.entry(classMember);
        return memberId;
    }

    public boolean isWhite(Integer courseId, String openid) {
        return courseFreeListDao.isFree(openid, courseId);
    }

    //生成学号 2位课程号2位班级号3位学号
    private String memberId(Integer courseId, Integer classId) {
        Integer classNumber = classDao.classNumber(courseId);
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

}
