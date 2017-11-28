package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import com.iquanwai.confucius.biz.util.page.Page;

import java.util.Date;
import java.util.List;

/**
 * Created by 三十文 on 2017/9/15
 */
public interface MonthlyCampService {
    List<RiseClassMember> loadRiseClassMemberByClassName(String className);

    List<RiseClassMember> loadUnGroupRiseClassMember(Page page);

    RiseClassMember updateRiseClassMemberById(RiseClassMember riseClassMember);

    int initRiseClassMember(RiseClassMember riseClassMember);

    RiseClassMember loadRiseClassMemberById(Integer riseClassMemberId);

    int batchUpdateRiseClassMemberByIds(List<Integer> riseMemberIds, String groupId);

    List<RiseClassMember> batchQueryRiseClassMemberByProfileIds(List<Integer> profileIds);

    boolean validForceOpenCourse(Integer month, Integer problemId);

    void batchForceOpenCourse(Integer problemId, Date startDate, Date closeDate);

    /**
     * 训练营切换人员数据处理<br/>
     * 1. 获取切换之前月份中的所有人，筛选出会员身份人员<br/>
     * 2. 将会员身份人员数据 copy 一份，更改对应年月数据，插入 RiseClassMember 表<br/>
     * 3. 将历史年月的人员 Active 字段设为 0<br/>
     * 4. 将切换之后年月的人员 Active 字段更新为 1
     * @param sourceYear 切换之前年份
     * @param sourceMonth 切换之前月份
     * @param targetYear 切换后年份
     * @param targetMonth 切换后月份
     */
    void switchCampDataProcess(Integer sourceYear, Integer sourceMonth, Integer targetYear, Integer targetMonth);

    void unlockMonthlyCampAuthority(String riseId);

    void insertRiseCertificate(Integer type, List<String> memberIds);
}
