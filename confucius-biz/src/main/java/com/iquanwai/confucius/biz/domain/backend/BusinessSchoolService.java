package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.po.common.customer.BusinessSchoolApplication;
import com.iquanwai.confucius.biz.po.common.survey.SurveyQuestionSubmit;
import com.iquanwai.confucius.biz.po.common.survey.SurveySubmit;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.util.page.Page;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/9/27.
 */
public interface BusinessSchoolService {
    String queryAnswerContentMapping(String label, String content);

    List<BusinessSchoolApplication> loadBusinessSchoolList(Page page);

    Boolean checkIsAsst(Integer profileId);

    Boolean rejectApplication(Integer applicationId,String comment);

    Boolean approveApplication(Integer applicationId, Double coupon, String comment);

    Boolean ignoreApplication(Integer applicationId, String comment);

    BusinessSchoolApplication loadBusinessSchoolApplication(Integer applicationId);

    String queryFinalPayStatus(Integer profileId);

    RiseMember getUserRiseMember(Integer profileId);

    Pair<SurveySubmit, List<SurveyQuestionSubmit>> loadSubmit(Integer submitId);

    /**
     * 获取用户最后一次审批通过的商学院申请的通过时间
     * */
    Date loadLastApplicationDealTime(Integer profileId);

    /**
     * 申请作废
     * */
    void expireApplication(Integer profileId);
}
