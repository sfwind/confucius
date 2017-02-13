package com.iquanwai.confucius.biz.domain.course.operational;

import com.iquanwai.confucius.biz.po.PromoCode;

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
     * @return -1 优惠码不可用
     *         >0 折扣金额
     * */
    Integer checkPromoCode(String promoCode);

    /**
     * 使用优惠码
     * @param openid 使用者id
     * @param promoCode 优惠码
     * */
    void usePromoCode(String openid, String promoCode);
}
