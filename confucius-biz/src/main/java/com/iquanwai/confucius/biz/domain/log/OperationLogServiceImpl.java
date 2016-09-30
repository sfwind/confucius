package com.iquanwai.confucius.biz.domain.log;

import com.iquanwai.confucius.biz.dao.OperationLogDao;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by justin on 16/9/3.
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {
    @Autowired
    private OperationLogDao operationLogDao;

    public void log(OperationLog operationLog) {
        if(ConfigUtils.logSwitch()) {
            operationLogDao.insert(operationLog);
        }
    }
}
