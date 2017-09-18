package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.dao.fragmentation.RiseClassMemberDao;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by 三十文 on 2017/9/15
 */
@Service
public class MonthlyCampServiceImpl implements MonthlyCampService {

    @Autowired
    RiseClassMemberDao riseClassMemberDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public List<RiseClassMember> loadRiseClassMemberByClassName(String className) {
        return riseClassMemberDao.loadByClassName(className);
    }

    @Override
    public List<RiseClassMember> loadUnGroupRiseClassMember() {
        return riseClassMemberDao.loadUnGroupMember();
    }

    @Override
    public RiseClassMember updateRiseClassMemberById(RiseClassMember riseClassMember) {
        int result = riseClassMemberDao.update(riseClassMember);
        if (result > 0) {
            return riseClassMemberDao.load(RiseClassMember.class, riseClassMember.getId());
        } else {
            return null;
        }
    }

    @Override
    public int batchUpdateRiseClassMemberByIds(List<Integer> riseMemberIds, String groupId) {
        return riseClassMemberDao.batchUpdateGroupId(riseMemberIds, groupId);
    }

}
