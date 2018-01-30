package com.iquanwai.confucius.biz.domain.asst;

import com.iquanwai.confucius.biz.po.asst.AsstUpDefault;
import com.iquanwai.confucius.biz.po.asst.AsstUpExecution;
import com.iquanwai.confucius.biz.po.asst.AsstUpStandard;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.util.page.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

/**
 * 助教评分service
 */
public interface AsstUpService {
    /**
     * 加载教练
     * @param page
     * @return
     */
    List<UserRole> loadAssists(Page page);

    List<AsstUpDefault> loadAssistDefault();

    /**
     * 根据教练级别获得对应的默认规则
     * @param roleId
     * @return
     */
    AsstUpDefault loadDefaultByRoleId(Integer roleId);
    /**
     * 加载助教升级标准
     * @param profileId
     * @return
     */
    AsstUpStandard loadStandard(Integer profileId);
    Integer insertStandard(AsstUpStandard asstUpStandard);
    Integer updateStandard(AsstUpStandard asstUpStandard);

    AsstUpExecution loadUpGradeExecution(Integer profileId);
    AsstUpExecution load(Integer id);
    Integer insertExecution(Integer standardId,Integer profileId,Integer roleId,Date startDate);
    Integer updateExecution(AsstUpExecution asstUpExecution);

    void updateExecution(MultipartFile file);



}
