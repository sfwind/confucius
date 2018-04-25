package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.dao.fragmentation.ApplicationPracticeDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ApplicationSubmitDao;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationPractice;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by nethunder on 2017/1/13.
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ApplicationPracticeDao applicationPracticeDao;
    @Autowired
    private ApplicationSubmitDao applicationSubmitDao;

    @Override
    public ApplicationPractice loadApplicationPractice(Integer id) {
        return applicationPracticeDao.load(ApplicationPractice.class, id);
    }

    @Override
    public ApplicationSubmit loadMineApplicationPractice(Integer planId, Integer applicationId, Integer profileId,
                                                         boolean create) {
        // 查询该应用练习
        ApplicationPractice applicationPractice = applicationPracticeDao.load(ApplicationPractice.class, applicationId);
        // 查询该用户是否提交
        ApplicationSubmit submit = applicationSubmitDao.load(applicationId, planId, profileId);
        if (submit == null && create) {
            // 没有提交，生成
            submit = new ApplicationSubmit();
            submit.setProfileId(profileId);
            submit.setPlanId(planId);
            submit.setApplicationId(applicationId);
            submit.setProblemId(applicationPractice.getProblemId());
            int submitId = applicationSubmitDao.insert(submit);
            submit.setId(submitId);
        } else {
            if (submit == null) {
                submit = new ApplicationSubmit();
            }
        }
        submit.setTopic(applicationPractice.getTopic());
        submit.setDescription(applicationPractice.getDescription());
        submit.setApplicationId(applicationPractice.getId());
        return submit;
    }

    @Override
    public List<ApplicationSubmit> loadApplicationSubmitList(Integer applicationId) {
        List<ApplicationSubmit> applicationSubmits = applicationSubmitDao.load(applicationId);
        applicationSubmits.stream().forEach(applicationSubmit -> {
            String content = CommonUtils.replaceHttpsDomainName(applicationSubmit.getContent());
            if (!content.equals(applicationSubmit.getContent())) {
                applicationSubmit.setContent(content);
                applicationSubmitDao.updateContent(applicationSubmit.getId(), content);
            }
        });

        return applicationSubmits;
    }

    @Override
    public ApplicationSubmit loadSubmit(Integer id) {
        return applicationSubmitDao.load(ApplicationSubmit.class, id);
    }

    @Override
    public Integer updateApplicationPractice(Integer id, String topic, String description,int difficulty) {
        return applicationPracticeDao.updateApplicationPracticeById(id, topic, description,difficulty);
    }

    @Override
    public int insertApplicationPractice(ApplicationPractice applicationPractice) {
        return applicationPracticeDao.insertApplicationPractice(applicationPractice);
    }


}
