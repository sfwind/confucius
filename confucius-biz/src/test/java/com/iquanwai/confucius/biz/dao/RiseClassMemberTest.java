package com.iquanwai.confucius.biz.dao;

import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by 三十文 on 2017/9/20
 */
public class RiseClassMemberTest extends TestBase {

    @Autowired
    private RiseMemberDao riseMemberDao;

    @Test
    public void load(){
        List<Integer> profileIds = riseMemberDao.loadEliteMembersId();
        System.out.println(profileIds.size());
    }
}
