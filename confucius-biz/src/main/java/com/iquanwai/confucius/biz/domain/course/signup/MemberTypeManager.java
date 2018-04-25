package com.iquanwai.confucius.biz.domain.course.signup;

import com.iquanwai.confucius.biz.po.fragmentation.MemberType;

import java.util.List;

/**
 * Created by nethunder on 2017/4/6.
 */
public interface MemberTypeManager {

    /**
     * 根据id获得memberType
     * @param memberTypeId 会员类型的id
     * @return 会员类型，如果该id无效，则返回null
     */
    MemberType memberType(Integer memberTypeId);

    List<MemberType> memberTypes();

    void reload();
}
