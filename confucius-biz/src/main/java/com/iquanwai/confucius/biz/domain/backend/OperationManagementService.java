package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.util.page.Page;

import java.util.List;

/**
 * Created by justin on 17/3/16.
 */
public interface OperationManagementService {

    /**
     * 根据应用id,获取应用训练提交记录
     * */
    List<ApplicationSubmit> loadApplicationSubmit(Integer practiceId, Page page);
}
