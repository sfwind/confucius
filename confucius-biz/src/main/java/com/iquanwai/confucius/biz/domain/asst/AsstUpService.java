package com.iquanwai.confucius.biz.domain.asst;

import com.iquanwai.confucius.biz.po.asst.AsstUpDefault;
import com.iquanwai.confucius.biz.po.asst.AsstUpExecution;
import com.iquanwai.confucius.biz.po.asst.AsstUpStandard;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.util.page.Page;

import java.util.List;

/**
 * 助教升降级service
 */
public interface AsstUpService {
    /**
     * 加载升降级完成情况
     * @param profileId
     * @return
     */
    AsstUpExecution loadUpGradeExecution(Integer profileId);
    Integer updateExecution(AsstUpExecution asstUpExecution);
    AsstUpExecution load(Integer id);
    /**
     * 加载助教升级标准
     * @param profileId
     * @return
     */
    AsstUpStandard loadStandard(Integer profileId);

    Integer updateStandard(AsstUpStandard asstUpStandard);

    /**
     * 加载教练
     * @param page
     * @return
     */
    List<UserRole> loadAssists(Page page);

    List<AsstUpDefault> loadAssistDefault();
}
