package com.iquanwai.confucius.biz.domain.asst;

import com.iquanwai.confucius.biz.dao.asst.AsstUpExecutionDao;
import com.iquanwai.confucius.biz.dao.asst.AsstUpStandardDao;
import com.iquanwai.confucius.biz.dao.common.permission.UserRoleDao;
import com.iquanwai.confucius.biz.po.asst.AsstUpExecution;
import com.iquanwai.confucius.biz.po.asst.AsstUpStandard;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.util.page.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsstUpServiceImpl implements AsstUpService{
    @Autowired
    private AsstUpStandardDao asstUpStandardDao;
    @Autowired
    private AsstUpExecutionDao asstUpExecutionDao;
    @Autowired
    private UserRoleDao userRoleDao;


    @Override
    public AsstUpExecution loadUpGradeExecution(Integer profileId) {
        return asstUpExecutionDao.queryByProfileId(profileId);
    }

    @Override
    public Integer updateExecution(AsstUpExecution asstUpExecution) {
        return null;
    }

    @Override
    public AsstUpStandard loadStandard(Integer profileId) {
        return asstUpStandardDao.queryByProfileId(profileId);
    }

    @Override
    public Integer updateStandard(AsstUpStandard asstUpStandard) {
       return asstUpStandardDao.update(asstUpStandard);
    }

    @Override
    public List<UserRole> loadAssists(Page page) {
        List<UserRole> userRoles = userRoleDao.loadAssistsList(page);
        page.setTotal(userRoleDao.loadAssistsCount());
        return userRoles;
    }
}
