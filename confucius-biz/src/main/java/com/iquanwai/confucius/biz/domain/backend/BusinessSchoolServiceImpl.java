package com.iquanwai.confucius.biz.domain.backend;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.common.customer.BusinessSchoolApplicationDao;
import com.iquanwai.confucius.biz.dao.common.customer.CustomerStatusDao;
import com.iquanwai.confucius.biz.dao.common.customer.MemberTypeDao;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.common.permission.UserRoleDao;
import com.iquanwai.confucius.biz.dao.common.survey.SurveyQuestionSubmitDao;
import com.iquanwai.confucius.biz.dao.common.survey.SurveySubmitDao;
import com.iquanwai.confucius.biz.dao.course.CouponDao;
import com.iquanwai.confucius.biz.dao.wx.QuanwaiOrderDao;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.common.customer.BusinessSchoolApplication;
import com.iquanwai.confucius.biz.po.common.customer.CustomerStatus;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.po.common.survey.SurveyQuestionSubmit;
import com.iquanwai.confucius.biz.po.common.survey.SurveySubmit;
import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.page.Page;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by nethunder on 2017/9/27.
 */
@Service
public class BusinessSchoolServiceImpl implements BusinessSchoolService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BusinessSchoolApplicationDao businessSchoolApplicationDao;
    @Autowired
    private SurveySubmitDao surveySubmitDao;
    @Autowired
    private SurveyQuestionSubmitDao surveyQuestionSubmitDao;
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
    private CouponDao couponDao;
    @Autowired
    private CustomerStatusDao customerStatusDao;

    private Map<String, Map<String, String>> mappings;


    @PostConstruct
    public void init() {
        mappings = Maps.newHashMap();
        Map<String, String> q1Map = Maps.newHashMap();
        Map<String, String> q2Map = Maps.newHashMap();
        Map<String, String> q3Map = Maps.newHashMap();
        Map<String, String> q4Map = Maps.newHashMap();
        Map<String, String> q6Map = Maps.newHashMap();
        Map<String, String> q12Map = Maps.newHashMap();

        mappings.put("q1", q1Map);
        mappings.put("q2", q2Map);
        mappings.put("q3", q3Map);
        mappings.put("q4", q4Map);
        mappings.put("q6", q6Map);
        mappings.put("q12", q12Map);
        q1Map.put("1", "互联网/电子商务");
        q1Map.put("2", "IT/软硬件服务");
        q1Map.put("3", "医疗健康");
        q1Map.put("4", "快速消费品(食品/饮料/化妆品)");
        q1Map.put("5", "耐用消费品(服饰家居/工艺玩具)");
        q1Map.put("6", "贸易零售");
        q1Map.put("7", "汽车及零配件制造");
        q1Map.put("8", "工业设备制造");
        q1Map.put("9", "通信电子");
        q1Map.put("10", "物流交通");
        q1Map.put("11", "能源化工");
        q1Map.put("12", "金融行业");
        q1Map.put("13", "管理咨询");
        q1Map.put("14", "法律");
        q1Map.put("15", "教育培训");
        q1Map.put("16", "餐饮娱乐");
        q1Map.put("17", "建筑地产");
        q1Map.put("18", "中介/猎头/认证服务");
        q1Map.put("19", "其他行业");

        q2Map.put("1", "互联网运营");
        q2Map.put("2", "互联网产品");
        q2Map.put("3", "研发/技术人员");
        q2Map.put("4", "销售");
        q2Map.put("5", "市场/公关");
        q2Map.put("6", "客户服务");
        q2Map.put("7", "人力资源");
        q2Map.put("8", "财务审计");
        q2Map.put("9", "行政后勤");
        q2Map.put("10", "生产运营");
        q2Map.put("11", "咨询顾问");
        q2Map.put("12", "律师");
        q2Map.put("13", "教师");
        q2Map.put("14", "全日制学生");
        q2Map.put("15", "专业人士(如记者、摄影师、医护人员等)");
        q2Map.put("16", "其他职业");

        q3Map.put("1", "普通员工");
        q3Map.put("2", "承担项目管理或临时管理权限的资深员工");
        q3Map.put("3", "一线主管");
        q3Map.put("4", "部门负责人");
        q3Map.put("5", "公司高管");
        q3Map.put("6", "CEO/公司创始人/董事");

        q4Map.put("1", "国有企业/事业单位");
        q4Map.put("2", "外资企业");
        q4Map.put("3", "合资企业");
        q4Map.put("4", "私营企业");
        q4Map.put("5", "其他");

        q6Map.put("1", "专科及以下");
        q6Map.put("2", "本科");
        q6Map.put("3", "硕士");
        q6Map.put("4", "博士及以上");

        q12Map.put("1", "是");
        q12Map.put("2", "否");

    }

    @Override
    public String queryAnswerContentMapping(String label, String content) {
        if (mappings.get(label) != null) {
            return mappings.get(label).get(content);
        } else {
            return null;
        }
    }


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

    private Boolean checkCoupon(Double coupon) {
//        if (coupon != null) {
//            return coupon > 0 && coupon <= 500;
//        } else {
//            return false;
//        }
        return coupon != null &&  coupon > 0;
    }

    @Override
    public Boolean approveApplication(Integer applicationId, Double coupon, String comment) {
        BusinessSchoolApplication application = businessSchoolApplicationDao.load(BusinessSchoolApplication.class, applicationId);
        if (application.getStatus() == BusinessSchoolApplication.APPLYING) {
            boolean result = businessSchoolApplicationDao.approve(applicationId, coupon, comment) > 0;
            if (result) {
                // 发放优惠券，开白名单
                // 是否有优惠券
                if (checkCoupon(coupon)) {
                    Coupon couponBean = new Coupon();
                    couponBean.setAmount(coupon);
                    couponBean.setOpenid(application.getOpenid());
                    couponBean.setProfileId(application.getProfileId());
                    couponBean.setUsed(0);
                    couponBean.setExpiredDate(DateUtils.afterDays(new Date(), 7));
                    couponBean.setCategory("ELITE_RISE_MEMBER");
                    couponBean.setDescription("商学院奖学金");
                    couponDao.insert(couponBean);
                }
                customerStatusDao.insert(application.getProfileId(), CustomerStatus.APPLY_BUSINESS_SCHOOL_SUCCESS);
            } else {
                logger.error("申请id：{} 审核通过处理失败,comment:{},coupon:{}", applicationId, comment, coupon);
            }
            return result;
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
    public Pair<SurveySubmit, List<SurveyQuestionSubmit>> loadSubmit(Integer submitId) {
        SurveySubmit submit = surveySubmitDao.load(SurveySubmit.class, submitId);
        List<SurveyQuestionSubmit> list = surveyQuestionSubmitDao.loadSubmitQuestions(submitId);
        return new MutablePair<>(submit, list);
    }
}
