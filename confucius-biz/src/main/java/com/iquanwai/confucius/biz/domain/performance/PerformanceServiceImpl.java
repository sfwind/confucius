package com.iquanwai.confucius.biz.domain.performance;

import com.iquanwai.confucius.biz.dao.performance.PagePerformanceDao;
import com.iquanwai.confucius.biz.po.performance.PagePerformance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by shen on 17/3/2.
 */
@Service
public class PerformanceServiceImpl implements PerformanceService {
    @Autowired
    private PagePerformanceDao pagePerformanceDao;
    @Override
    public void add(PagePerformance po) {
        pagePerformanceDao.entry(po);
    }
}
