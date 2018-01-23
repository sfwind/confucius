package com.iquanwai.confucius.web.course.dto.backend;

import com.iquanwai.confucius.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.confucius.biz.po.apply.InterviewRecord;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/9/28.
 * 返回给前端的商学院申请Dto
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

    List<BusinessApplyQuestion> questionList;


    private Integer submitId;
    private Integer profileId;
    private String checkTime; // 审核时间
    private Boolean deal; // 技术是否处理
    private Integer originMemberType; // 申请时的会员类型
    private String originMemberTypeName;
    private String reward;
    private Boolean del;
    private String verifiedResult;
    private Integer interviewer;
    private String interviewerName;

    /**
     * 申请id
     */
    private Integer applyId;
    /**
     * 面试时间
     */
    private String interviewTime;
    /**
     * 工作时间
     */
    private String workYear;
    /**
     * 工作职责
     */
    private String industry;
    /**
     * 学历
     */
    private String education;

    /**
     * 院校名称
     */
    private String college;

    /**
     * 所在地
     */
    private String location;
    /**
     * 当前行业
      */
    private String job;

    /**
     * 面试记录
     */
    private InterviewRecord interviewRecord;

    /**
     * 是否已经面试过
     */
    private String isInterviewed;

}
