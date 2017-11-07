package com.iquanwai.confucius.biz.domain.backend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.fragmentation.MonthlyCampScheduleDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseClassMemberDao;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampSchedule;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.util.page.Page;
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
import java.util.Map;
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
    public List<RiseClassMember> loadUnGroupRiseClassMember(Page page) {
        List<RiseClassMember> unGroupRiseClassMembers = riseClassMemberDao.loadUnGroupMember();
        page.setTotal(unGroupRiseClassMembers.size());
        return riseClassMemberDao.loadUnGroupMemberPage(page);
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
    public void batchForceOpenCourse(Integer problemId, Date startDate, Date closeDate) {
        List<RiseClassMember> riseClassMembers = riseClassMemberDao.loadActiveRiseClassMembers();
        List<Integer> profileIds = riseClassMembers.stream().map(RiseClassMember::getProfileId).collect(Collectors.toList());

        JSONObject json = new JSONObject();
        json.put("problemId", problemId);
        json.put("startDate", startDate);
        json.put("closeDate", closeDate);

        for (Integer profileId : profileIds) {
            json.put("profileId", profileId);
            try {
                forceOpenPublisher.publish(json.toString());
            } catch (ConnectException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    public void switchCampDataProcess(Integer sourceYear, Integer sourceMonth, Integer targetYear, Integer targetMonth) {
        // 获取切换之前月份的所有人员
        List<RiseClassMember> sourceRiseClassMembers = riseClassMemberDao.loadAllByYearMonth(sourceYear, sourceMonth);
        List<RiseMember> sourceRiseMembers = riseMemberDao.loadByProfileIds(sourceRiseClassMembers.stream().map(RiseClassMember::getProfileId).collect(Collectors.toList()));
        Map<Integer, RiseMember> sourceRiseMemberMap = sourceRiseMembers.stream().collect(Collectors.toMap(RiseMember::getProfileId, riseMember -> riseMember));

        sourceRiseClassMembers.forEach(riseClassMember -> {
            // 筛选出其中的仍是商学院的人员
            Integer profileId = riseClassMember.getProfileId();
            RiseMember riseMember = sourceRiseMemberMap.get(profileId);
            if (RiseMember.ELITE == riseMember.getMemberTypeId() || RiseMember.HALF_ELITE == riseMember.getMemberTypeId()) {
                RiseClassMember existRiseClassMember = riseClassMemberDao.queryByProfileIdAndTime(profileId, targetYear, targetMonth);
                if (existRiseClassMember == null) {
                    RiseClassMember targetRiseClassMember = JSON.parseObject(JSON.toJSONString(riseClassMember), RiseClassMember.class);
                    targetRiseClassMember.setYear(targetYear);
                    targetRiseClassMember.setMonth(targetMonth);
                    targetRiseClassMember.setActive(0);
                    riseClassMemberDao.insert(targetRiseClassMember);
                }
            }
        });

        // 将上个月的所有数据 Active 置为 0，表示已过期
        int updateResult = riseClassMemberDao.batchUpdateActive(sourceYear, sourceMonth, 0);
        // 将下个月的所有数据 Active 置为 1，表示新的月份生效
        if (updateResult > 0) {
            riseClassMemberDao.batchUpdateActive(targetYear, targetMonth, 1);
        }
    }

}
