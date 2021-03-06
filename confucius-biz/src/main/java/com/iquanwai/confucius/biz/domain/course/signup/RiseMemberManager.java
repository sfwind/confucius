package com.iquanwai.confucius.biz.domain.course.signup;


import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by justin on 2018/4/7.
 */
public interface RiseMemberManager {

    /**
     * 商学院核心能力项目
     */
    RiseMember coreBusinessSchoolMember(Integer profileId);

    /**
     * 专项课用户
     */
    RiseMember campMember(Integer profileId);

    /**
     * 商业思维类用户
     */
    RiseMember businessThought(Integer profileId);

    /**
     * 专业版用户
     */
    RiseMember proMember(Integer profileId);

    /**
     * 老用户（商学院+专业版）
     */
    RiseMember oldMember(Integer profileId);

    /**
     * 所有用户信息
     */
    List<RiseMember> member(Integer profileId);

    /**
     * 商学院用户信息
     */
    List<RiseMember> businessSchoolMember(Integer profileId);


    /**
     * 获取用户所有的用户信息
     */
    List<RiseMember> loadPersonalAllRiseMembers(Integer profileId);

    Pair<Integer,Integer> loadWannaGoodsIdByApplyId(Integer applyMemberId);

    Pair<Integer,Integer> loadApplyIdByGoodsId(Integer wannaGoodsId);

    List<RiseMember> loadValidRiseMembers(Integer profileId);
}
