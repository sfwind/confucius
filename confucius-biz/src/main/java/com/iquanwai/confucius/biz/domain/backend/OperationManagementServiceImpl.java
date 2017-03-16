package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.dao.fragmentation.ApplicationSubmitDao;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.util.page.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by justin on 17/3/16.
 */
@Service
public class OperationManagementServiceImpl implements OperationManagementService {
    @Autowired
    private ApplicationSubmitDao applicationSubmitDao;
    @Override
    public List<ApplicationSubmit> loadApplicationSubmit(Integer practiceId, Page page) {
        return applicationSubmitDao.getPracticeSubmit(practiceId, page);
    }
}
