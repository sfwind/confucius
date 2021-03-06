package com.iquanwai.confucius.biz.domain.backend;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.fragmentation.CourseScheduleDefaultDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ProblemDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseCertificateDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseClassMemberDao;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private RiseCertificateDao riseCertificateDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private ProblemDao problemDao;
    @Autowired
    private CourseScheduleDefaultDao courseScheduleDefaultDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public List<RiseClassMember> loadRiseClassMemberByClassName(String className) {
        return riseClassMemberDao.loadByClassName(className);
    }

    @Override
    public List<RiseClassMember> batchQueryRiseClassMemberByProfileIds(List<Integer> profileIds) {
        return riseClassMemberDao.batchQueryByProfileIds(profileIds);
    }

    @Override
    public void switchCampDataProcess(Integer sourceYear, Integer sourceMonth, Integer targetYear, Integer targetMonth) {
        logger.info("开启切换专项课数据");

        // 获取切换之前月份的所有人员
        List<RiseClassMember> sourceRiseClassMembers = riseClassMemberDao.loadAllByYearMonth(sourceYear, sourceMonth);
        List<RiseMember> sourceRiseMembers = riseMemberDao.loadByProfileIds(sourceRiseClassMembers.stream().map(RiseClassMember::getProfileId).collect(Collectors.toList()));
        Map<Integer, RiseMember> sourceRiseMemberMap = sourceRiseMembers.stream().collect(Collectors.toMap(RiseMember::getProfileId, riseMember -> riseMember));

        sourceRiseClassMembers.forEach(riseClassMember -> {
            logger.info("正在处理:" + riseClassMember.getMemberId());
            // 筛选出其中的仍是商学院的人员
            Integer profileId = riseClassMember.getProfileId();
            RiseMember riseMember = sourceRiseMemberMap.get(profileId);
            if (riseMember != null && (RiseMember.ELITE == riseMember.getMemberTypeId() || RiseMember.HALF_ELITE == riseMember.getMemberTypeId())) {
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
        logger.info("专项课数据切换完毕");
    }

    /**
     * 在 RiseCertificate 表中，初始化需要发送的人员数据
     */
    @Override
    public void insertRiseCertificate(Integer year, Integer month, Integer type, List<String> memberIds) {
        List<Profile> profiles = accountService.getProfilesByMemberIds(memberIds);
        List<Integer> certificateNoSequence = Lists.newArrayList();
        certificateNoSequence.add(1);

        profiles.forEach(profile -> {
            logger.info("正在添加：" + profile.getMemberId());
            Integer profileId = profile.getId();

            List<RiseCertificate> riseCertificates = riseCertificateDao.loadRiseCertificatesByProfileId(profileId);
            RiseCertificate existRiseCertificate = riseCertificates.stream()
                    .filter(riseCertificate -> riseCertificate.getType().equals(type)
                            && riseCertificate.getYear().equals(year)
                            && riseCertificate.getMonth().equals(month))
                    .findAny().orElse(null);

            // 如果该类型的证书已经添加过，则不再添加
            if (existRiseCertificate == null) {
                RiseClassMember riseClassMember = riseClassMemberDao.queryByProfileIdAndTime(profileId, year, month);

                Integer category = accountService.loadUserScheduleCategory(profileId);
                List<CourseScheduleDefault> courseScheduleDefaults = courseScheduleDefaultDao
                        .loadMajorCourseScheduleDefaultByCategory(category);
                CourseScheduleDefault courseScheduleDefault = courseScheduleDefaults.stream()
                        .filter(scheduleDefault -> scheduleDefault.getType() == CourseScheduleDefault.Type.MAJOR)
                        .filter(scheduleDefault -> scheduleDefault.getMonth().equals(month)).findAny().orElse(null);

                String problemName = "";
                if (courseScheduleDefault != null) {
                    Integer problemId = courseScheduleDefault.getProblemId();
                    Problem problem = problemDao.load(Problem.class, problemId);
                    if (problem != null) {
                        problemName = problem.getAbbreviation();
                    }
                }

                StringBuilder certificateNoBuilder = new StringBuilder("IQW");
                certificateNoBuilder.append(String.format("%02d", type));
                certificateNoBuilder.append(profile.getMemberId());
                certificateNoBuilder.append(String.format("%02d", month));
                Integer noSequence = certificateNoSequence.get(0);
                certificateNoSequence.clear();
                certificateNoSequence.add(noSequence + 1);
                certificateNoBuilder.append(String.format("%03d", noSequence));
                certificateNoBuilder.append(String.format("%02d", RandomUtils.nextInt(0, 100)));

                RiseCertificate riseCertificate = new RiseCertificate();
                riseCertificate.setProfileId(profileId);
                riseCertificate.setType(type);
                riseCertificate.setCertificateNo(certificateNoBuilder.toString());
                riseCertificate.setYear(year);
                riseCertificate.setMonth(month);
                try {
                    riseCertificate.setGroupNo(Integer.parseInt(riseClassMember.getGroupId()));
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
                riseCertificate.setProblemName(problemName);
                riseCertificateDao.insert(riseCertificate);
            }
        });
    }

}
