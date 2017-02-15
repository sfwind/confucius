package com.iquanwai.confucius.biz.domain.course.operational;

import com.iquanwai.confucius.biz.po.PromoCode;

import java.util.List;

/**
 * Created by justin on 17/2/13.
 */
public interface PromoCodeService {
    /**
     * 获取优惠码
     * @param openid 用户id
     * */
    PromoCode getPromoCode(String openid);

    /**
     * 校验优惠码
     * @param promoCode 优惠码
     * @return -1.0 优惠码不可用
     *         >0 折扣金额
     * */
    Double discount(String promoCode);

    /**
     * 使用优惠码
     * @param openid 使用者id
     * @param promoCode 优惠码
     * */
    void usePromoCode(String openid, String promoCode);

    /**
     * 根据活动码获取优惠码
     * @param activityCode 活动码
     * */
    List<PromoCode> getPromoCodes(String activityCode);
}
