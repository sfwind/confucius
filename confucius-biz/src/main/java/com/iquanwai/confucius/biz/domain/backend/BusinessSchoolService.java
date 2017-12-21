package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.confucius.biz.po.common.customer.BusinessSchoolApplication;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.util.page.Page;

import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/9/27.
 */
public interface BusinessSchoolService {
    /**
     * 获取商学院申请提交列表
     * */
    List<BusinessSchoolApplication> loadBusinessSchoolList(Page page);
    /**
     * 判断是否是助教
     * */
    Boolean checkIsAsst(Integer profileId);
    /**
     * 拒绝申请
     * */
    Boolean rejectApplication(Integer applicationId,String comment);
    /**
     * 同意申请
     * */
    Boolean approveApplication(Integer applicationId, Double coupon, String comment);
    /**
     * 忽略申请
     * */
    Boolean ignoreApplication(Integer applicationId, String comment);
    /**
     * 获取某个申请
     * */
    BusinessSchoolApplication loadBusinessSchoolApplication(Integer applicationId);
    /**
     * 查询支付状态
     * */
    String queryFinalPayStatus(Integer profileId);
    /**
     * 查询会员状态
     * */
    RiseMember getUserRiseMember(Integer profileId);

    /**
     * 获取用户最后一次审批通过的商学院申请的通过时间
     * */
    Date loadLastApplicationDealTime(Integer profileId);

    /**
     * 申请作废
     * */
    void expireApplication(Integer profileId);

    /**
     * 获取用户申请信息
     *
     * @param applyId 申请id
     * @return 申请信息
     */
    List<BusinessApplyQuestion> loadUserQuestions(Integer applyId);

    /**
     * 查询用户是否是试听课优秀学员
     * @param profileId 用户id
     * @return 身份信息
     */
    String loadUserAuditionReward(Integer profileId);

    /**
     * 获取正在检查中的记录
     * @param profileId 用户id
     * @return
     */
    BusinessSchoolApplication loadCheckingApply(Integer profileId);
}
