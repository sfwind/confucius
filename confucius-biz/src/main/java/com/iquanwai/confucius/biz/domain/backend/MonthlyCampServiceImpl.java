package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.dao.RedisUtil;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseClassMemberDao;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by 三十文 on 2017/9/15
 */
@Service
public class MonthlyCampServiceImpl implements MonthlyCampService {

    @Autowired
    private AccountService accountService;
    @Autowired
    private RiseClassMemberDao riseClassMemberDao;
    @Autowired
    private RedisUtil redisUtil;

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
    public int initRiseClassMember(RiseClassMember riseClassMember) {
        return riseClassMemberDao.insert(riseClassMember);
    }

    @Override
    public RiseClassMember loadRiseClassMemberById(Integer riseClassMemberId) {
        return riseClassMemberDao.load(RiseClassMember.class, riseClassMemberId);
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

    @Override
    public List<RiseClassMember> batchQueryRiseClassMemberByProfileIds(List<Integer> profileIds) {
        return riseClassMemberDao.batchQueryByProfileIds(profileIds);
    }

    /**
     * 生成 memberId，格式 YYYYMM + 6位数字
     */
    private String generateMemberId() {
        StringBuilder targetMemberId = new StringBuilder();

        String prefix = ConfigUtils.getMemberIdPrefix();

        String key = "customer:memberId:" + prefix;
        redisUtil.lock("lock:memberId", (lock) -> {
            // TODO 有效期 60 天，期间 redis 绝对不能重启！！！
            String memberId = redisUtil.get(key);
            String sequence;
            if (StringUtils.isEmpty(memberId)) {
                sequence = "000001";
            } else {
                sequence = String.format("%06d", Integer.parseInt(memberId) + 1);
            }
            targetMemberId.append(prefix).append(sequence);
            redisUtil.set(key, sequence, DateUtils.afterDays(new Date(), 60).getTime());
        });
        return targetMemberId.toString();
    }

}
