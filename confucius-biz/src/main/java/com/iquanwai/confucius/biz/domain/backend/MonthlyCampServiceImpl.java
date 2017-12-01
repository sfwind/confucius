package com.iquanwai.confucius.biz.domain.backend;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseCertificateDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseClassMemberDao;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.fragmentation.CacheService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.RiseCertificate;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.util.page.Page;
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
    private SignupService signupService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private RiseClassMemberDao riseClassMemberDao;
    @Autowired
    private RiseCertificateDao riseCertificateDao;
    @Autowired
    private RiseMemberDao riseMemberDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

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
    public void switchCampDataProcess(Integer sourceYear, Integer sourceMonth, Integer targetYear, Integer targetMonth) {
        // 获取切换之前月份的所有人员
        List<RiseClassMember> sourceRiseClassMembers = riseClassMemberDao.loadAllByYearMonth(sourceYear, sourceMonth);
        List<RiseMember> sourceRiseMembers = riseMemberDao.loadByProfileIds(sourceRiseClassMembers.stream().map(RiseClassMember::getProfileId).collect(Collectors.toList()));
        Map<Integer, RiseMember> sourceRiseMemberMap = sourceRiseMembers.stream().collect(Collectors.toMap(RiseMember::getProfileId, riseMember -> riseMember));

        sourceRiseClassMembers.forEach(riseClassMember -> {
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
    }

    @Override
    public void unlockMonthlyCampAuthority(String riseId) {
        Profile profile = accountService.getProfileByRiseId(riseId);
        signupService.unlockMonthlyCamp(profile.getId());
    }

    @Override
    public void insertRiseCertificate(Integer type, List<String> memberIds) {
        List<RiseClassMember> riseClassMembers = riseClassMemberDao.loadByMemberIds(memberIds);

        List<Integer> certificateNoSequence = Lists.newArrayList();
        certificateNoSequence.add(1);

        riseClassMembers.forEach(riseClassMember -> {
            logger.info("正在添加：" + riseClassMember.getMemberId());
            Integer profileId = riseClassMember.getProfileId();
            Integer year = riseClassMember.getYear();
            Integer month = riseClassMember.getMonth();

            List<RiseCertificate> riseCertificates = riseCertificateDao.loadRiseCertificatesByProfileId(profileId);
            RiseCertificate existRiseCertificate = riseCertificates.stream()
                    .filter(riseCertificate -> riseCertificate.getType().equals(type)
                            && riseCertificate.getYear().equals(year)
                            && riseCertificate.getMonth().equals(month))
                    .findAny().orElse(null);

            // 如果该类型的证书已经添加过，则不再添加
            if (existRiseCertificate == null) {
                String groupNo = riseClassMember.getGroupId();

                Integer category = accountService.loadUserScheduleCategory(profileId);
                List<CourseScheduleDefault> courseScheduleDefaults = courseScheduleDefaultDao.loadByCategory(category);
                CourseScheduleDefault courseScheduleDefault = courseScheduleDefaults.stream()
                        .filter(scheduleDefault -> scheduleDefault.getType() == CourseScheduleDefault.Type.MAJOR)
                        .filter(scheduleDefault -> scheduleDefault.getMonth().equals(month)).findAny().orElse(null);
                String problemName = "";
                if (courseScheduleDefault != null) {
                    Integer problemId = courseScheduleDefault.getProblemId();
                    Problem problem = problemDao.load(Problem.class, problemId);
                    if (problem != null) {
                        problemName = problem.getProblem();
                    }
                }

                StringBuilder certificateNoBuilder = new StringBuilder("IQW");
                certificateNoBuilder.append(String.format("%02d", type));
                certificateNoBuilder.append(riseClassMember.getMemberId());
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
                    riseCertificate.setGroupNo(Integer.parseInt(groupNo));
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
                riseCertificate.setProblemName(problemName);
                riseCertificateDao.insert(riseCertificate);
            }
        });
    }

}
