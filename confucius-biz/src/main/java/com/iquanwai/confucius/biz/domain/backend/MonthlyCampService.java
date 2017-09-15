package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;

import java.util.List;

/**
 * Created by 三十文 on 2017/9/15
 */
public interface MonthlyCampService {
    List<RiseClassMember> loadMonthlyCampByClassName(String className);

    List<RiseClassMember> loadUnGroupRiseClassMember();

    RiseClassMember modifyMonthlyCampByClassName(RiseClassMember riseClassMember);
}
