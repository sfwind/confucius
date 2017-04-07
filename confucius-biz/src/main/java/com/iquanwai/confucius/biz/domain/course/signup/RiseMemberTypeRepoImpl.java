package com.iquanwai.confucius.biz.domain.course.signup;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Created by nethunder on 2017/4/6.
 */
@Service
public class RiseMemberTypeRepoImpl implements RiseMemberTypeRepo {
    private static Map<Integer, MemberType> memberTypes = Maps.newConcurrentMap();
    @PostConstruct
    public void init(){
        List<Double> riseMemberPrice = ConfigUtils.getRiseMemberPrice();
        if (CollectionUtils.isNotEmpty(riseMemberPrice) && CollectionUtils.size(riseMemberPrice) == 3) {
            memberTypes.put(1, new MemberType(1, riseMemberPrice.get(0), "线上半年498"));
            memberTypes.put(2, new MemberType(2, riseMemberPrice.get(1), "线上一年898"));
            memberTypes.put(3, new MemberType(3, riseMemberPrice.get(2), "线上一年+线下+学习报告1980"));
        } else {
            memberTypes.put(1, new MemberType(1, 498D, "线上半年498"));
            memberTypes.put(2, new MemberType(2, 898D, "线上一年898"));
            memberTypes.put(3, new MemberType(3, 1980D, "线上一年+线下+学习报告1980"));
        }
    }

    @Override
    public MemberType memberType(Integer memberTypeId) {
        return memberTypes.get(memberTypeId);
    }
}