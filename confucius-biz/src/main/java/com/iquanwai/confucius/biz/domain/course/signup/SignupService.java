package com.iquanwai.confucius.biz.domain.course.signup;

import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.fragmentation.BusinessSchoolApplicationOrder;
import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampOrder;
import com.iquanwai.confucius.biz.po.fragmentation.RiseOrder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by justin on 16/8/29.
 */
public interface SignupService {

    /**
     * 商品支付资格校验
     */
    Pair<Boolean, String> risePurchaseCheck(Integer profileId, Integer memberType);

    /**
     * 报名商学院, 不生成预付订单
     */
    QuanwaiOrder signUpRiseMember(Integer profileId, Integer memberTypeId, List<Integer> couponId, Integer payType);

    /**
     * 报名专项课, 不生成预付订单
     */
    QuanwaiOrder signUpMonthlyCamp(Integer profileId, Integer memberTypeId, Integer couponId, Integer payType);

    /**
     * 申请商学院, 不生成预付订单
     */
    QuanwaiOrder signupBusinessSchoolApplication(Integer profileId, Integer memberTypeId, Integer couponId, Integer payType);

    /**
     * 获取专项课订单
     */
    MonthlyCampOrder getMonthlyCampOrder(String orderId);


    /**
     * 获得圈外订单
     *
     * @param orderId 订单id
     */
    QuanwaiOrder getQuanwaiOrder(String orderId);

    /**
     * 获得rise订单
     *
     * @param orderId 订单id
     */
    RiseOrder getRiseOrder(String orderId);


    /**
     * 获取用户可以使用的优惠券
     */
    List<Coupon> getCoupons(Integer profileId);


    /**
     * 查询会员类型的支付信息
     */
    MemberType getMemberTypePayInfo(Integer profileId, Integer memberTypeId);

    /**
     * 计算优惠券
     *
     * @param memberTypeId 会员id
     * @param couponId     优惠券id
     * @return 打的折扣是多少
     */
    Double calculateMemberCoupon(Integer profileId, Integer memberTypeId, List<Integer> couponId);


    /**
     * 获得该会员对应月份主修课id
     *
     * @param profileId 用户id
     * @param month     月份
     * @return 主修课程id
     */
    Integer loadHrefProblemId(Integer profileId, Integer month);


    /**
     * 获取商学院申请订单
     *
     * @param orderId 订单号
     * @return 商学院申请订单
     */
    BusinessSchoolApplicationOrder getBusinessSchoolOrder(String orderId);


    /**
     * 根据商品类型和售价智能选择优惠券
     *
     * @param goodsType 商品类型
     * @param fee       价格
     * @param coupons   优惠券
     * @return
     */
    List<Coupon> autoChooseCoupon(String goodsType, Double fee, List<Coupon> coupons);

    /**
     * 获取该售卖页qrcode
     *
     * @param memberTypeId 售卖类型
     * @return qrcode链接
     */
    String getSubscribeQrCodeForPay(Integer memberTypeId);

    /**
     * 设置项目的剩余名额
     *
     * @param remainNumber 剩余人数
     * @param memberTypeId 身份id
     */
    void changeRemainNumber(Integer remainNumber, Integer memberTypeId);

}
