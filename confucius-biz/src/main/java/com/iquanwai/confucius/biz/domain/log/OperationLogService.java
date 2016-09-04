package com.iquanwai.confucius.biz.domain.log;

import com.iquanwai.confucius.biz.po.OperationLog;

/**
 * Created by justin on 16/9/3.
 */
public interface OperationLogService {

    void log(OperationLog operationLog);
}
