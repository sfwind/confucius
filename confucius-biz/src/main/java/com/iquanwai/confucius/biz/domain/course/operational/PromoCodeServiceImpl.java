package com.iquanwai.confucius.biz.domain.course.operational;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.course.CouponDao;
import com.iquanwai.confucius.biz.dao.operational.PromoCodeDao;
import com.iquanwai.confucius.biz.dao.operational.PromoCodeUsageDao;
import com.iquanwai.confucius.biz.domain.course.signup.CostRepo;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.PromoCode;
import com.iquanwai.confucius.biz.po.PromoCodeUsage;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 17/2/14.
 */
@Service
public class PromoCodeServiceImpl implements PromoCodeService{
    @Autowired
    private PromoCodeDao promoCodeDao;
    @Autowired
    private PromoCodeUsageDao promoCodeUsageDao;
    @Autowired
    private CouponDao couponDao;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CostRepo costRepo;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public PromoCode getPromoCode(Integer id) {
        return promoCodeDao.load(PromoCode.class, id);
    }

    @Override
    public PromoCode getPromoCode(String openid) {
        //TODO:需要不断切换活动码,目前只支持一个running的活动
        PromoCode promoCode = promoCodeDao.queryPromoCodeByOwner(openid, ActivityFactory.ACTIVITY_CAREER_COURSE_PACKAGE);
        if (isValid(promoCode)) {
            return promoCode;
        }

        return null;
    }

    private boolean isValid(PromoCode promoCode) {
        //优惠码存在且没过期
        if(promoCode!=null && promoCode.getExpiredDate()!=null && promoCode.getExpiredDate().after(new Date())){
            return true;
        }
        return false;
    }

    @Override
    public Double discount(String code) {
        PromoCode promoCode = promoCodeDao.queryPromoCode(code, ActivityFactory.ACTIVITY_CAREER_COURSE_PACKAGE);
        if (isValid(promoCode)) {
            return promoCode.getDiscount();
        }
        return -1.0;
    }

    @Override
    public void usePromoCode(String openid, String code) {
        Activity activity = ActivityFactory.getActivity(ActivityFactory.ACTIVITY_CAREER_COURSE_PACKAGE);
        if(activity==null){
            logger.error("活动 {} 不存在", ActivityFactory.ACTIVITY_CAREER_COURSE_PACKAGE);
            return;
        }
        logger.info("使用优惠券{}", code);
        careerActivity(activity, openid, code);

    }

    @Override
    public List<PromoCode> getPromoCodes(String activityCode) {
        return promoCodeDao.queryPromoCodeByActivityCode(activityCode);
    }

    private void careerActivity(Activity activity, String openid, String code){
        PromoCode promoCode = promoCodeDao.queryPromoCode(code, activity.getName());
        if(promoCode==null){
            logger.error("优惠码{}不存在", code);
            return;
        }
        if(!isValid(promoCode)){
            logger.error("优惠码{}已过期", code);
            return;
        }
        if(promoCode.getUseCount() < activity.getPromoCodeUsageLimit()){
            synchronized (this){
                promoCode = promoCodeDao.queryPromoCode(code, activity.getName());
                if(promoCode.getUseCount() < activity.getPromoCodeUsageLimit()){
                    //插入优惠券
                    Coupon coupon = new Coupon();
                    coupon.setOpenid(promoCode.getOwner());
                    coupon.setAmount(promoCode.getDiscount());
                    coupon.setUsed(0);
                    //过期日期是结束日期+1
                    coupon.setExpiredDate(DateUtils.afterDays(activity.getEndDate(),1));
                    couponDao.insert(coupon);
                    //发送优惠码折扣通知
                    sendCouponMsg(promoCode, activity, openid);
                    costRepo.reloadCache();
                }
            }
        }
        //优惠码使用次数+1
        promoCodeDao.incrementPromoCodeUsage(code, activity.getName());
        PromoCodeUsage promoCodeUsage = new PromoCodeUsage();
        promoCodeUsage.setPromoCodeId(promoCode.getId());
        promoCodeUsage.setUser(openid);
        promoCodeUsageDao.insert(promoCodeUsage);
    }

    private void sendCouponMsg(PromoCode promoCode, Activity activity, String openid) {
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(promoCode.getOwner());
        templateMessage.setTemplate_id(ConfigUtils.accountChangeMsgKey());
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();

        Account account = accountService.getAccount(openid, true);
        String nickname = account.getNickname();
        data.put("first",new TemplateMessage.Keyword("恭喜，你的优惠码已被"+nickname+"成功使用！", TemplateMessage.BLACK));
        data.put("keyword1",new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
        data.put("keyword2",new TemplateMessage.Keyword("这个春天，一起来重新学习职业发展！"));
        int discount_percent = 20*(promoCode.getUseCount()+1);
        data.put("keyword3",new TemplateMessage.Keyword("已优惠"+discount_percent+"%"));
        if(discount_percent==100) {
            data.put("remark", new TemplateMessage.Keyword("恭喜，你已免费获得一门职业发展课程\n" +
                    "点击下方训练营，可免费报名“求职背后的秘密”或“战略性职业规划”任意一门"));
        }else{
            data.put("remark", new TemplateMessage.Keyword("你现在的课程优惠价格为"+(45-activity.getDiscount().intValue()*(promoCode.getUseCount()+1))+"元\n" +
                    "距离免费听课只剩"+(activity.getPromoCodeUsageLimit()-promoCode.getUseCount()-1)+"次"));
        }
        templateMessage.setData(data);

        templateMessageService.sendMessage(templateMessage);
    }

    public static void main(String[] args) {

        for(int i=0;i<2;i++){
            String code = CommonUtils.randomString(4).toUpperCase();
            while (code.contains("1")||code.contains("0")
                    ||code.contains("O")||code.contains("I")){
                code = CommonUtils.randomString(4).toUpperCase();
            }
            System.out.println(code);
        }
    }
}
