package com.iquanwai.confucius.biz.domain.course.signup;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by justin on 2018/4/7.
 */
@Service
public class RiseMemberManagerImpl implements RiseMemberManager {
    @Autowired
    private RiseMemberDao riseMemberDao;

    private List<Pair<Integer, Integer>> applyMapping = Lists.newArrayList();

    @PostConstruct
    public void init() {
        applyMapping.clear();
        applyMapping.add(Pair.of(RiseMember.BS_APPLICATION, RiseMember.ELITE));
        applyMapping.add(Pair.of(RiseMember.BUSINESS_THOUGHT_APPLY, RiseMember.BUSINESS_THOUGHT));
    }

    @Override
    public RiseMember coreBusinessSchoolMember(Integer profileId) {
        List<Integer> members = Lists.newArrayList();
        members.add(RiseMember.ELITE);
        members.add(RiseMember.HALF_ELITE);

        return getRiseMember(profileId, members);
    }

    private RiseMember getRiseMember(Integer profileId, List<Integer> members) {
        List<RiseMember> riseMembers = riseMemberDao.loadValidRiseMemberByMemberTypeId(profileId, members);
        if (CollectionUtils.isEmpty(riseMembers)) {
            return null;
        }

        return riseMembers.get(0);
    }

    @Override
    public RiseMember campMember(Integer profileId) {
        List<Integer> members = Lists.newArrayList();
        members.add(RiseMember.CAMP);

        return getRiseMember(profileId, members);
    }

    @Override
    public RiseMember businessThought(Integer profileId) {
        List<Integer> members = Lists.newArrayList();
        members.add(RiseMember.BUSINESS_THOUGHT);

        return getRiseMember(profileId, members);
    }

    @Override
    public RiseMember proMember(Integer profileId) {
        List<Integer> members = Lists.newArrayList();
        members.add(RiseMember.HALF);
        members.add(RiseMember.ANNUAL);

        return getRiseMember(profileId, members);
    }

    @Override
    public RiseMember oldMember(Integer profileId) {
        List<Integer> members = Lists.newArrayList();
        members.add(RiseMember.HALF);
        members.add(RiseMember.ANNUAL);
        members.add(RiseMember.HALF_ELITE);
        members.add(RiseMember.ELITE);

        return getRiseMember(profileId, members);
    }

    @Override
    public List<RiseMember> member(Integer profileId) {
        List<Integer> members = Lists.newArrayList();
        members.add(RiseMember.HALF);
        members.add(RiseMember.ANNUAL);
        members.add(RiseMember.HALF_ELITE);
        members.add(RiseMember.ELITE);
        members.add(RiseMember.CAMP);
        members.add(RiseMember.BUSINESS_THOUGHT);

        return riseMemberDao.loadValidRiseMemberByMemberTypeId(profileId, members);
    }

    @Override
    public List<RiseMember> businessSchoolMember(Integer profileId) {
        List<Integer> members = Lists.newArrayList();
        members.add(RiseMember.HALF_ELITE);
        members.add(RiseMember.ELITE);
        members.add(RiseMember.BUSINESS_THOUGHT);

        return riseMemberDao.loadValidRiseMemberByMemberTypeId(profileId, members);
    }

    @Override
    public List<RiseMember> loadPersonalAllRiseMembers(Integer profileId) {
        return riseMemberDao.loadPersonalAll(profileId);
    }

    @Override
    public Pair<Integer, Integer> loadWannaGoodsIdByApplyId(Integer applyMemberId) {
        return this.applyMapping.stream().filter(item -> item.getLeft().equals(applyMemberId)).findAny().orElse(null);
    }

    @Override
    public Pair<Integer, Integer> loadApplyIdByGoodsId(Integer wannaGoodsId) {
        return this.applyMapping.stream().filter(item -> item.getRight().equals(wannaGoodsId)).findAny().orElse(null);
    }

    @Override
    public List<RiseMember> loadValidRiseMembers(Integer profileId) {
        return riseMemberDao.loadValidRiseMembers(profileId);
    }


}
