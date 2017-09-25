package com.iquanwai.confucius.biz.domain.backend;

import com.alibaba.fastjson.JSONObject;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.fragmentation.MonthlyCampScheduleDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseClassMemberDao;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampSchedule;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.ConnectException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by 三十文 on 2017/9/15
 */
@Service
public class MonthlyCampServiceImpl implements MonthlyCampService {

    @Autowired
    private RiseClassMemberDao riseClassMemberDao;
    @Autowired
    private MonthlyCampScheduleDao monthlyCampScheduleDao;
    @Autowired
    private RiseMemberDao riseMemberDao;

    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    private RabbitMQPublisher forceOpenPublisher;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String FORCE_OPEN = "monthly_camp_force_open_topic";

    @PostConstruct
    public void init() {
        forceOpenPublisher = rabbitMQFactory.initFanoutPublisher(FORCE_OPEN);
    }

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
        Integer profileId = riseClassMember.getProfileId();
        List<RiseClassMember> classMembers = riseClassMemberDao.queryByProfileId(profileId);
        if (classMembers.size() == 0) {
            return riseClassMemberDao.insert(riseClassMember);
        } else {
            return -1;
        }
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

    @Override
    public boolean validForceOpenCourse(Integer month, Integer problemId) {
        List<MonthlyCampSchedule> schedules = monthlyCampScheduleDao.loadByMonth(month);
        List<Integer> problemIds = schedules.stream().map(MonthlyCampSchedule::getProblemId).collect(Collectors.toList());
        return problemIds.contains(problemId);
    }

    @Override
    public void batchForceOpenCourse(Integer problemId, Date closeDate) {
        List<Integer> profileIds = riseMemberDao.loadEliteMembersId();

        JSONObject json = new JSONObject();
        json.put("problemId", problemId);
        json.put("closeDate", closeDate);

        for (Integer profileId : profileIds) {
            json.put("profileId", profileId);
            try {
                forceOpenPublisher.publish(json.toString());
            } catch (ConnectException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
