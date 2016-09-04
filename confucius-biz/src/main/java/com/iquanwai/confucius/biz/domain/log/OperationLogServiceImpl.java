package com.iquanwai.confucius.biz.domain.log;

import com.iquanwai.confucius.biz.dao.OperationLogDao;
import com.iquanwai.confucius.biz.po.OperationLog;
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
        operationLogDao.insert(operationLog);
    }
}
