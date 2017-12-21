package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.dao.apply.AuditionRewardDao;
import com.iquanwai.confucius.biz.dao.apply.BusinessApplyQuestionDao;
import com.iquanwai.confucius.biz.dao.apply.BusinessApplySubmitDao;
import com.iquanwai.confucius.biz.dao.apply.BusinessSchoolApplicationDao;
import com.iquanwai.confucius.biz.dao.common.customer.CustomerStatusDao;
import com.iquanwai.confucius.biz.dao.common.customer.MemberTypeDao;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.common.permission.UserRoleDao;
import com.iquanwai.confucius.biz.dao.wx.QuanwaiOrderDao;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.apply.AuditionReward;
import com.iquanwai.confucius.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.confucius.biz.po.apply.BusinessApplySubmit;
import com.iquanwai.confucius.biz.po.common.customer.BusinessSchoolApplication;
import com.iquanwai.confucius.biz.po.common.customer.CustomerStatus;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.util.page.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        Profile profile = profileDao.load(Profile.class, profileId);
        QuanwaiOrder order = quanwaiOrderDao.loadCampOrBusinessOrder(profile.getOpenid());
        if (riseMember == null) {
            // 查看是否点过付费按钮
            if (order != null) {
                return "点击付费按钮未付费";
            } else {
                return "未点击付费按钮";
            }
        } else {
            if (riseMember.getMemberTypeId().equals(RiseMember.ELITE)) {
                return "已付费商学院";
            } else if (riseMember.getMemberTypeId().equals(RiseMember.CAMP)) {
                return "已付费训练营";
            } else {
                if (order != null) {
                    return "点击付费按钮未付费";
                } else {
                    return "未点击付费按钮";
                }
            }
        }
    }

    @Override
    public RiseMember getUserRiseMember(Integer profileId) {
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        if (riseMember != null) {
            MemberType memberType = memberTypeDao.load(MemberType.class, riseMember.getMemberTypeId());
            if (memberType != null) {
                riseMember.setName(memberType.getName());
            }
        }
        return riseMember;
    }

    @Override
    public Date loadLastApplicationDealTime(Integer profileId) {
        BusinessSchoolApplication businessSchoolApplication = businessSchoolApplicationDao
                .loadLastApproveApplication(profileId);
        if (businessSchoolApplication != null) {
            Date dealTime = businessSchoolApplication.getDealTime();
            //如果申请通过通知还未发,则返回通过时间,反之则返回通知时间
            if (dealTime == null) {
                return businessSchoolApplication.getCheckTime();
            } else {
                return dealTime;
            }
        } else {
            return null;
        }
    }

    @Override
    public void expireApplication(Integer profileId) {
        customerStatusDao.delStatus(profileId, CustomerStatus.APPLY_BUSINESS_SCHOOL_SUCCESS);
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
}
