package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.dao.apply.AuditionRewardDao;
import com.iquanwai.confucius.biz.dao.apply.BusinessApplyChoiceDao;
import com.iquanwai.confucius.biz.dao.apply.BusinessApplyQuestionDao;
import com.iquanwai.confucius.biz.dao.apply.BusinessApplySubmitDao;
import com.iquanwai.confucius.biz.dao.apply.BusinessSchoolApplicationDao;
import com.iquanwai.confucius.biz.dao.common.customer.CustomerStatusDao;
import com.iquanwai.confucius.biz.dao.common.customer.MemberTypeDao;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.common.permission.UserRoleDao;
import com.iquanwai.confucius.biz.dao.common.permission.WhiteListDao;
import com.iquanwai.confucius.biz.dao.wx.QuanwaiOrderDao;
import com.iquanwai.confucius.biz.domain.course.signup.RiseMemberManager;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.apply.AuditionReward;
import com.iquanwai.confucius.biz.po.apply.BusinessApplyChoice;
import com.iquanwai.confucius.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.confucius.biz.po.apply.BusinessApplySubmit;
import com.iquanwai.confucius.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.page.Page;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2017/9/27.
 */
@Service
public class BusinessSchoolServiceImpl implements BusinessSchoolService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BusinessSchoolApplicationDao businessSchoolApplicationDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private MemberTypeDao memberTypeDao;
    @Autowired
    private QuanwaiOrderDao quanwaiOrderDao;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private CustomerStatusDao customerStatusDao;
    @Autowired
    private BusinessApplyQuestionDao businessApplyQuestionDao;
    @Autowired
    private BusinessApplySubmitDao businessApplySubmitDao;
    @Autowired
    private AuditionRewardDao auditionRewardDao;
    @Autowired
    private WhiteListDao whiteListDao;
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private BusinessApplyChoiceDao businessApplyChoiceDao;
    @Autowired
    private OperationLogService operationLogService;

    @Override
    public List<BusinessSchoolApplication> loadBusinessSchoolList(Page page) {
        List<BusinessSchoolApplication> submits = businessSchoolApplicationDao.loadList(page);
        page.setTotal(businessSchoolApplicationDao.loadCount());
        return submits;
    }

    @Override
    public Boolean checkIsAsst(Integer profileId) {
        List<UserRole> roles = userRoleDao.getRoles(profileId);
        Optional<UserRole> role = roles.stream().filter(item -> item.getRoleId().equals(3) || item.getRoleId().equals(4) || item.getRoleId().equals(11)).findFirst();
        return role.isPresent();
    }

    @Override
    public Boolean rejectApplication(Integer applicationId, String comment) {
        return businessSchoolApplicationDao.reject(applicationId, comment) > 0;
    }

    @Override
    public Boolean approveApplication(Integer applicationId, Double coupon, String comment) {
        BusinessSchoolApplication application = businessSchoolApplicationDao.load(BusinessSchoolApplication.class, applicationId);
        if (application.getStatus() == BusinessSchoolApplication.APPLYING) {
            return businessSchoolApplicationDao.approve(applicationId, coupon, comment) > 0;
        } else {
            logger.error("申请id：{} 重复执行通过操作,comment:{},coupon:{}", applicationId, comment, coupon);
            return false;
        }
    }

    @Override
    public Boolean ignoreApplication(Integer applicationId, String comment) {
        return businessSchoolApplicationDao.ignore(applicationId, comment) > 0;
    }

    @Override
    public BusinessSchoolApplication loadBusinessSchoolApplication(Integer applicationId) {
        return businessSchoolApplicationDao.load(BusinessSchoolApplication.class, applicationId);
    }

    @Override
    public String queryFinalPayStatus(Integer profileId) {
        List<RiseMember> riseMembers = riseMemberManager.loadValidRiseMembers(profileId);


        QuanwaiOrder order = quanwaiOrderDao.loadCampOrBusinessOrder(profileId);
        if (CollectionUtils.isEmpty(riseMembers)) {
            // 查看是否点过付费按钮
            if (order != null) {
                return "点击付费按钮未付费";
            } else {
                return "未点击付费按钮";
            }
        } else {
            if(riseMembers.contains(RiseMember.ELITE)){
                return "已付费商学院";
            }else if(riseMembers.contains(RiseMember.CAMP)){
                return "已付费专项课";
            }else if(riseMembers.contains(RiseMember.BUSINESS_THOUGHT)){
                return "已付费商业进阶课程";
            } else{
                if (order != null) {
                    return "点击付费按钮未付费";
                } else {
                    return "未点击付费按钮";
                }
            }
        }
    }

    @Override
    public String getUserRiseMemberNames(Integer profileId) {
        List<RiseMember> riseMembers = riseMemberManager.loadValidRiseMembers(profileId);
        StringBuilder stringBuilder = new StringBuilder();

        if(CollectionUtils.isNotEmpty(riseMembers)){
           riseMembers.forEach(riseMember -> {
               MemberType memberType = memberTypeDao.load(MemberType.class,riseMember.getMemberTypeId());
               if(memberType!=null){
                   stringBuilder.append(memberType.getName()+" ");
               }
           });
        }
        return stringBuilder.toString();
    }


    @Override
    public List<BusinessApplyQuestion> loadUserQuestions(Integer applyId) {
        List<BusinessApplySubmit> submits = businessApplySubmitDao.loadByApplyId(applyId);
        return businessApplyQuestionDao.loadAll(BusinessApplyQuestion.class)
                .stream()
                .filter(item -> submits
                        .stream()
                        .anyMatch(submit -> item.getId().equals(submit.getQuestionId()))
                )
                .peek(item -> submits
                        .stream()
                        .filter(submit -> item.getId().equals(submit.getQuestionId()))
                        .findFirst()
                        .ifPresent(userSubmit -> {
                            if (userSubmit.getChoiceText() != null) {
                                item.setAnswer(userSubmit.getChoiceText());
                            } else {
                                item.setAnswer(userSubmit.getUserValue());
                            }
                        })
                )
                .collect(Collectors.toList());
    }

    @Override
    public String loadUserAuditionReward(Integer profileId) {
        AuditionReward auditionReward = auditionRewardDao.loadByProfileId(profileId);
        if (auditionReward != null) {
            if (auditionReward.getIdentity().equals(AuditionReward.Identity.COMMITTEE)) {
                return "班委";
            } else if (auditionReward.getIdentity().equals(AuditionReward.Identity.WINNINGGROUP)) {
                return "优秀学员";
            }
        }
        return "否";
    }

    @Override
    public BusinessSchoolApplication loadCheckingApply(Integer profileId) {
        return businessSchoolApplicationDao.loadCheckingApplication(profileId);
    }

    @Override
    public List<UserRole> loadInterviewer() {
        List<String> interviewers = Arrays.asList(ConfigUtils.getInterviewers().split(","));

        return interviewers.stream().map(item -> {
            Integer profileId = Integer.parseInt(item);
            UserRole role = userRoleDao.loadAssist(profileId);
            if (role != null) {
                return role;
            } else {
                role = new UserRole();
                role.setProfileId(profileId);
                return role;
            }
        }).collect(Collectors.toList());
    }

    @Override
    public Integer assignInterviewer(Integer applyId, Integer interviewer) {
        return businessSchoolApplicationDao.assignInterviewer(applyId, interviewer);
    }

    @Override
    public List<BusinessApplySubmit> loadByApplyId(Integer applyId) {
        return businessApplySubmitDao.loadByApplyId(applyId);
    }

    @Override
    public void submitBusinessApply(Integer profileId, List<BusinessApplySubmit> userApplySubmits, Boolean valid, Integer goodsId) {
        //获取上次审核的结果
        BusinessSchoolApplication lastBusinessApplication = businessSchoolApplicationDao.getLastVerifiedByProfileId(profileId);

        BusinessSchoolApplication application = new BusinessSchoolApplication();
        application.setProfileId(profileId);
        application.setSubmitTime(new Date());
        application.setStatus(BusinessSchoolApplication.APPLYING);

        application.setIsDuplicate(false);
        application.setValid(valid);
        application.setDeal(false);
        if (goodsId == RiseMember.BS_APPLICATION) {
            application.setMemberTypeId(Constants.MemberType.ELITE);
        } else if (goodsId == RiseMember.BUSINESS_THOUGHT_APPLY) {
            application.setMemberTypeId(Constants.MemberType.THOUGHT);
        }
        if (lastBusinessApplication != null) {
            application.setLastVerified(lastBusinessApplication.getStatus());
        } else {
            application.setLastVerified(0);
        }

        // TODO:待验证
        Optional<RiseMember> optional = riseMemberManager.member(profileId).stream()
                .sorted(((o1, o2) -> o2.getId() - o1.getId())).findFirst();
        optional.ifPresent(riseMember -> application.setOriginMemberType(riseMember.getMemberTypeId()));

        Integer applyId = businessSchoolApplicationDao.insert(application);

        userApplySubmits.forEach(item -> {
            item.setApplyId(applyId);
            if (item.getChoiceId() != null) {
                BusinessApplyChoice choice = businessApplyChoiceDao.load(BusinessApplyChoice.class, item.getChoiceId());
                item.setChoiceText(choice.getSubject() == null ? "异常数据" : choice.getSubject());
            }
        });
        businessApplySubmitDao.batchInsertApplySubmit(userApplySubmits);

        operationLogService.trace(profileId, "submitApply");
    }

    @Override
    public void expiredApply(Integer id) {
        businessSchoolApplicationDao.expiredApply(id);
    }
}
