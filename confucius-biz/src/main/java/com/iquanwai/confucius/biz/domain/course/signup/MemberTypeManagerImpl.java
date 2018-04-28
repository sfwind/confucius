package com.iquanwai.confucius.biz.domain.course.signup;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.common.customer.MemberTypeDao;
import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2017/4/6.
 */
@Service
public class MemberTypeManagerImpl implements MemberTypeManager {
    @Autowired
    private MemberTypeDao memberTypeDao;

    private static Map<Integer, MemberType> memberTypes = Maps.newHashMap();
    private static Logger logger = LoggerFactory.getLogger(MemberTypeManagerImpl.class);

    @PostConstruct
    public void init(){
        List<MemberType> types = memberTypeDao.loadAll(MemberType.class).stream().filter(item -> !item.getDel())
                .collect(Collectors.toList());

        types.forEach(item -> memberTypes.put(item.getId(), item));
        logger.info("圈外会员价格:{}", MemberTypeManagerImpl.memberTypes);
    }

    @Override
    public MemberType memberType(Integer memberTypeId) {
        return memberTypes.get(memberTypeId)==null?null: memberTypes.get(memberTypeId).copy();
    }

    @Override
    public List<MemberType> memberTypes() {
        return memberTypes.values().stream().map(MemberType::copy).collect(Collectors.toList());
    }

    @Override
    public void reload(){
        this.init();
    }
}