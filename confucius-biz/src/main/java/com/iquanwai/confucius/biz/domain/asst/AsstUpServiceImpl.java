package com.iquanwai.confucius.biz.domain.asst;

import com.iquanwai.confucius.biz.dao.asst.AsstUpDefaultDao;
import com.iquanwai.confucius.biz.dao.asst.AsstUpExecutionDao;
import com.iquanwai.confucius.biz.dao.asst.AsstUpStandardDao;
import com.iquanwai.confucius.biz.dao.common.permission.UserRoleDao;
import com.iquanwai.confucius.biz.po.asst.AsstUpDefault;
import com.iquanwai.confucius.biz.po.asst.AsstUpExecution;
import com.iquanwai.confucius.biz.po.asst.AsstUpStandard;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.util.page.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AsstUpServiceImpl implements AsstUpService{
    @Autowired
    private AsstUpDefaultDao asstUpDefaultDao;
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
        return asstUpExecutionDao.update(asstUpExecution);
    }

    @Override
    public AsstUpExecution load(Integer id) {
        return asstUpExecutionDao.load(AsstUpExecution.class,id);
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

    @Override
    public List<AsstUpDefault> loadAssistDefault() {
        List<AsstUpDefault> asstUpDefaults = asstUpDefaultDao.loadAll(AsstUpDefault.class);
        return asstUpDefaults.stream().filter(asstUpDefault -> asstUpDefault.getDel() == 0).collect(Collectors.toList());
    }

    @Override
    public AsstUpDefault loadDefaultByRoleId(Integer roleId) {
        return asstUpDefaultDao.queryByRoleId(roleId);
    }

    @Override
    public Integer insertStandard(AsstUpStandard asstUpStandard) {
        return asstUpStandardDao.insert(asstUpStandard);
    }

    @Override
    public Integer insertExecution(Integer standardId,Integer profileId, Integer roleId,Date startDate) {
        return asstUpExecutionDao.insert(standardId,profileId,roleId,startDate);
    }
}
