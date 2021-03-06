package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.confucius.biz.po.apply.BusinessApplySubmit;
import com.iquanwai.confucius.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.util.page.Page;

import java.util.List;

/**
 * Created by nethunder on 2017/9/27.
 */
public interface BusinessSchoolService {
    /**
     * 获取商学院申请提交列表
     */
    List<BusinessSchoolApplication> loadBusinessSchoolList(Page page);

    /**
     * 判断是否是助教
     */
    Boolean checkIsAsst(Integer profileId);

    /**
     * 拒绝申请
     */
    Boolean rejectApplication(Integer applicationId, String comment);

    /**
     * 同意申请
     */
    Boolean approveApplication(Integer applicationId, Double coupon, String comment);

    /**
     * 忽略申请
     */
    Boolean ignoreApplication(Integer applicationId, String comment);

    /**
     * 获取某个申请
     */
    BusinessSchoolApplication loadBusinessSchoolApplication(Integer applicationId);

    /**
     * 查询支付状态
     */
    String queryFinalPayStatus(Integer profileId);

    /**
     * 查询会员状态
     */
   String getUserRiseMemberNames(Integer profileId);


    /**
     * 获取用户申请信息
     *
     * @param applyId 申请id
     * @return 申请信息
     */
    List<BusinessApplyQuestion> loadUserQuestions(Integer applyId);

    /**
     * 查询用户是否是试听课优秀学员
     *
     * @param profileId 用户id
     * @return 身份信息
     */
    String loadUserAuditionReward(Integer profileId);

    /**
     * 获取正在检查中的记录
     *
     * @param profileId 用户id
     * @return 审核中的记录
     */
    BusinessSchoolApplication loadCheckingApply(Integer profileId);

    /**
     * 获取所有面试官
     *
     * @return 助教列表
     */
    List<UserRole> loadInterviewer();

    /**
     * 分配审核人
     *
     * @param applyId     申请id
     * @param interviewer 审核人
     * @return 分配结果
     */
    Integer assignInterviewer(Integer applyId, Integer interviewer);

    /**
     * 加载商学院申请提交数据
     */
    List<BusinessApplySubmit> loadByApplyId(Integer applyId);


    void submitBusinessApply(Integer profileId, List<BusinessApplySubmit> userApplySubmits, Boolean valid, Integer goodsId);

    void expiredApply(Integer id);
}
