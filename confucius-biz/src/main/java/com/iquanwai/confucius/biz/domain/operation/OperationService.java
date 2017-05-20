package com.iquanwai.confucius.biz.domain.operation;


/**
 * Created by xfduan on 2017/5/20.
 */
public interface OperationService {

    /**
     * 查看学员是否已经获取过优惠券
     */
    Boolean alreadyGetDiscount(String openId);

    /**
     * 根据学员不同类型生成随机折扣金额
     */
    Integer getDiscountValue(String openId);

}
