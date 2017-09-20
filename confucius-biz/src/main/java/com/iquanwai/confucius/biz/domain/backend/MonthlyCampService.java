package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;

import java.util.Date;
import java.util.List;

/**
 * Created by 三十文 on 2017/9/15
 */
public interface MonthlyCampService {
    List<RiseClassMember> loadRiseClassMemberByClassName(String className);

    List<RiseClassMember> loadUnGroupRiseClassMember();

    RiseClassMember updateRiseClassMemberById(RiseClassMember riseClassMember);

    int initRiseClassMember(RiseClassMember riseClassMember);

    RiseClassMember loadRiseClassMemberById(Integer riseClassMemberId);

    int batchUpdateRiseClassMemberByIds(List<Integer> riseMemberIds, String groupId);

    List<RiseClassMember> batchQueryRiseClassMemberByProfileIds(List<Integer> profileIds);

    boolean validForceOpenCourse(Integer month, Integer problemId);

    void batchForceOpenCourse(Integer problemId, Date closeDate);
}
