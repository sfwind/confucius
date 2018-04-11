package com.iquanwai.confucius.biz.domain.fragmentation;

import com.iquanwai.confucius.biz.po.fragmentation.course.BusinessSchoolConfig;
import com.iquanwai.confucius.biz.po.fragmentation.course.CourseConfig;
import com.iquanwai.confucius.biz.po.fragmentation.course.MonthlyCampConfig;

public interface CacheService {
    /**
     * 获得商学院配置
     *
     * @param memberTypeId 会员id
     * @return
     */
    BusinessSchoolConfig loadBusinessCollegeConfig(Integer memberTypeId);

    /**
     * 获得专项课配置
     *
     * @return 专项课配置
     */
    MonthlyCampConfig loadMonthlyCampConfig();

    /**
     * 重新初始化缓存
     */
    void reload();

    /**
     * 重新加载专项课配置
     */
    void reloadMonthlyCampConfig();

    /**
     * 重新记载商学院配置
     */
    void reloadBusinessCollegeConfig();

    /**
     * 加载课程配置
     *
     * @param memberTypeId 会员id
     * @return 课程配置
     */
    CourseConfig loadCourseConfig(Integer memberTypeId);
}
