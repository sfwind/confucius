package com.iquanwai.confucius.biz.domain.log;

import com.iquanwai.confucius.biz.dao.common.log.OperationLogDao;
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

    @Override
    public void log(OperationLog operationLog) {
        if(ConfigUtils.logSwitch()) {
            if (operationLog.getMemo() != null && operationLog.getMemo().length() > 1024) {
                operationLog.setMemo(operationLog.getMemo().substring(0, 1024));
            }
            operationLogDao.insert(operationLog);
        }
    }
}
