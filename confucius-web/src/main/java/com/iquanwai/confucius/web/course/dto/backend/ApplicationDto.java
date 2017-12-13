package com.iquanwai.confucius.web.course.dto.backend;

import com.iquanwai.confucius.biz.po.apply.BusinessApplyQuestion;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/9/28.
 */
@Data
public class ApplicationDto {
    private Integer id;
    /**
     * 最终支付状态
     */
    private String finalPayStatus;
    /**
     *  审核状态
     *  1-通过，2-拒绝，3-私信
     */
    private Integer status;
    /**
     * 奖学金金额
     */
    private String coupon;
    private String openid;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 是否重复提交
     */
    private String isDuplicate;
    /**
     * 是否是助教
     */
    private String isAsst;
    /**
     * 是否是黑名单用户
     */
    private String isBlack;
    /**
     * 会员类型id
     */
    private Integer memberTypeId;
    /**
     * 会员类型
     */
    private String memberType;
    /**
     * 提交答卷时间
     */
    private String submitTime;
    /**
     * 所用时间
     */
    private String timeTaken;
    /**
     * 备注
     */
    private String comment;
    /**
     * 订单id
     */
    private String orderId;

    private String q1Answer; // 1. 请选择您目前从事的行业
    private String q2Answer; // 2. 请选择您目前从事的职业：
    private String q3Answer; // 3. 请选择您的职位层级：
    private String q4Answer; //4. 请选择您目前所在公司的企业性质：
    private String q5Answer;     // 	5. 请填写您的工作年限（请填写数字，暂无工作经验请填0）
    private String q6Answer;     // 6. 请选择您的最高学历
    private String q7Answer;     // 7.1 请选择您获取最高学历的国内高校名称：
    private String q8Answer;     // 7.2 如您毕业于海外高校，请填写您所毕业的海外高校名称：
    private String q9Answer;     // 8.1 请选择你所在的国内城市（方便我们邀请您加入校友会和当地活动）:
    private String q10Answer;     // 	8.2 如果您长期居住在海外，请填写你所在的海外国家和城市:
    private String q11Answer;     // 9. 请描述您加入圈外商学院的成长目标（如果您有其他任何有助于申请的信息，也可以在此留言告诉我们）
    private String q12Answer;     // 10. 您是否要申请奖学金？（奖学金视申请词而定，在100-300不等。极少数特别有诚意者，将有机会半免；奖学金申请结果将与商学院申请结果共同发放）
    private String q13Answer;     // 11. 请填写您的奖学金申请词，告诉我们：为什么应该给你奖学金
    private String q14Answer;     // 12. 请输入您的手机号码（仅用于重要申请消息通知，不会泄露给第三方或用于其他商业用途）：
    private String q15Answer;     // 13. 您的微信号（非微信昵称，而是微信ID）：

    List<BusinessApplyQuestion> questionList;


    private Integer submitId;
    private Integer profileId;
    private String checkTime; // 审核时间
    private Boolean deal; // 技术是否处理
    private Integer originMemberType; // 申请时的会员类型
    private String originMemberTypeName;
    private String reward;
    private Boolean del;
    private String lastVerified;
}
