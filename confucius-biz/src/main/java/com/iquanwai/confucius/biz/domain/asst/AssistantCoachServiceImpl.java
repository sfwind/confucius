package com.iquanwai.confucius.biz.domain.asst;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.apply.BusinessSchoolApplicationDao;
import com.iquanwai.confucius.biz.dao.apply.InterviewRecordDao;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.common.permission.UserRoleDao;
import com.iquanwai.confucius.biz.dao.fragmentation.*;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.RiseWorkInfoDto;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.ProfileCount;
import com.iquanwai.confucius.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.confucius.biz.po.apply.InterviewRecord;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.HtmlRegexpUtil;
import com.iquanwai.confucius.biz.util.page.Page;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author justin
 * @version 17/4/26
 */
@Service
public class AssistantCoachServiceImpl implements AssistantCoachService {
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private ApplicationSubmitDao applicationSubmitDao;
    @Autowired
    private ApplicationPracticeDao applicationPracticeDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AsstCoachCommentDao asstCoachCommentDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private RiseClassMemberDao riseClassMemberDao;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private BusinessSchoolApplicationDao businessSchoolApplicationDao;
    @Autowired
    private InterviewRecordDao interviewRecordDao;

    private static final int SIZE = 100;

    private static final int APPLICTION_SIZE = 200;

    //最近30天
    private static final int PREVIOUS_DAY = 30;

    @Override
    public Pair<Integer, Integer> getCommentCount(Integer profileId) {
        List<Comment> commentList = commentDao.loadCommentsByProfileId(profileId);
        // 将时间逆序排序按时间根据module和referenceId去重
        Long totalCommentCnt = commentList.stream().sorted(Comparator.comparing(Comment::getAddTime))
                .map(comment -> comment.getReferencedId().toString() + "-" + comment.getModuleId().toString()).distinct().count();
        // 将时间逆序列表过滤出其中日期为今日的评论并根据module和referenceId去重
        List<Comment> sortedComment = commentList.stream().sorted(Comparator.comparing(Comment::getAddTime)).collect(Collectors.toList());
        Map<String, Comment> filterMap = Maps.newHashMap();
        sortedComment.forEach(comment -> {
            filterMap.putIfAbsent(comment.getReferencedId().toString() + "-" + comment.getModuleId().toString(), comment);
        });
        Long todayCommentCnt = Lists.newArrayList(filterMap.values()).stream().filter(comment -> DateUtils.isToday(comment.getAddTime()))
                .distinct().count();

        return new ImmutablePair<>(todayCommentCnt.intValue(), totalCommentCnt.intValue());
    }


    //每5道题能被点评一次,按时间倒序，只评论30天内
    //1.获取未点评的作业,去掉求点评的作业
    //2.计算需要露出的作业数量
    //3 满足5题一次都没被点评，优先级最高
    //4.两个月之内要过期的优先级比较高

    @Override
    public List<RiseWorkInfoDto> getUnderCommentApplications(Integer problemId) {
        List<RiseWorkInfoDto> underCommentArticles = Lists.newArrayList();
        //找出课程的所有应用练习包括删除的
        List<ApplicationPractice> applicationPractices = applicationPracticeDao.getAllPracticeByProblemId(problemId);
        //只评论30天内的文章
        Date date = DateUtils.beforeDays(new Date(), PREVIOUS_DAY);

        //获取求点评的文章
        List<ApplicationSubmit> applicationSubmitList = Lists.newArrayList();
        List<ApplicationSubmit> list = applicationSubmitDao.loadRequestCommentApplications(problemId, SIZE);
        // 求点评的放在最上边
        // level1
        applicationSubmitList.addAll(list);
        if (applicationSubmitList.size() < SIZE) {
            // level2
            List<ProfileCount> asstCoachComments = asstCoachCommentDao.loadCommented(problemId);
            //已被教练评价用户id
            List<Integer> coachProfileIds = asstCoachComments.stream().map(ProfileCount::getProfileId).collect(Collectors.toList());
            // 取出最近一个月的作业,上限3000条
            List<ApplicationSubmit> baseSubmits = applicationSubmitDao.loadUnderCommentApplicationsExcludeSomeone(problemId, 3000, date, coachProfileIds
                    .stream().limit(3000).filter(item -> !coachProfileIds.contains(item)).collect(Collectors.toList()));
            // 删掉内容，减轻内存压力
            baseSubmits.forEach(item -> item.setContent(null));
            // 每个人提交的作业
            Map<Integer, List<ApplicationSubmit>> submitGroup = baseSubmits.stream().collect(Collectors.groupingBy(ApplicationSubmit::getProfileId));
            // 计算作业题数量，这些人是超过5个的,不超过5个按照1个算
            Map<Integer, Integer> userSubmitCount = applicationSubmitDao.loadUserSubmitCount(problemId);
            // 需要被点评的个数
            Map<Integer, Integer> shouldComment = Maps.newHashMap();
            userSubmitCount.keySet().forEach(item -> {
                int shouldCount = Double.valueOf(Math.ceil(userSubmitCount.get(item) / 5.0d)).intValue();
                Optional<ProfileCount> comment = asstCoachComments.stream().filter(tempItem -> tempItem.getProfileId().equals(item)).findFirst();
                int commentCount = comment.isPresent() ? comment.get().getCount() : 0;
                if (commentCount < shouldCount) {
                    shouldComment.put(item, shouldCount - commentCount);
                }
            });
            // 必须被评论的人
            List<Integer> mustComment = Lists.newArrayList();
            // 教练点评的人里没有这些提交超过五个，
            mustComment.addAll(userSubmitCount.keySet().stream().filter(item -> !coachProfileIds.contains(item)).collect(Collectors.toList()));
            // 从这些人中选作业
            mustComment.stream()
                    // 这个月提交了作业
                    .filter(submitGroup::containsKey)
                    .forEach(item -> applicationSubmitList.addAll(
                            submitGroup.get(item)
                                    .stream()
                                            // 越晚提交优先级越高
                                    .sorted(((o1, o2) -> o1.getPublishTime().before(o2.getPublishTime()) ? 1 : -1))
                                            // 如果已经满了则空转，否则取出需要被点评的数量
                                    .limit(applicationSubmitList.size() < SIZE ? (shouldComment.get(item) != null ? shouldComment.get(item) : 1) : 0)
                                    .collect(Collectors.toList())));
            if (applicationSubmitList.size() < SIZE) {
                // level3
                // 两个月之内要过期的人
                List<Integer> willExpired = riseMemberDao.loadByWillExpired(DateUtils.afterMonths(new Date(), 2))
                        .stream()
                                // 按时间排序，越早过期的优先级越高
                        .sorted((o1, o2) -> o1.getExpireDate().equals(o2.getExpireDate()) ? 0 : (o1.getExpireDate().before(o2.getExpireDate()) ? -1 : 1))
                        .map(RiseMember::getProfileId)
                                // 这个快过期的人不在必须被评论的list里，这个在上面已经被排进去了
                        .filter(item -> !mustComment.contains(item))
                        .collect(Collectors.toList());
                // 从这些快过期的人中选作业
                willExpired.stream()
                        // 他们最近一个月提交过作业
                        .filter(submitGroup::containsKey)
                        .forEach(item -> applicationSubmitList.addAll(submitGroup.get(item)
                                .stream()
                                        // 越晚提交被露出概率越高
                                .sorted(((o1, o2) -> o1.getPublishTime().before(o2.getPublishTime()) ? 1 : -1))
                                        // 如果没满则取
                                .limit(applicationSubmitList.size() < SIZE ? (shouldComment.get(item) != null ? shouldComment.get(item) : 1) : 0)
                                .collect(Collectors.toList())));
                if (applicationSubmitList.size() < SIZE) {
                    // level4
                    // 如果还没满的话，再从shouldComment里取
                    List<Integer> existProfileIds = applicationSubmitList.stream().map(ApplicationSubmit::getProfileId).distinct().collect(Collectors.toList());
                    shouldComment.keySet().stream()
                            // 没有露出作业 并且这个月提交了作业
                            .filter(item -> !existProfileIds.contains(item) && submitGroup.keySet().contains(item))
                            .forEach(item -> applicationSubmitList.addAll(submitGroup.get(item)
                                    .stream()
                                            // 越晚提交被露出概率越高
                                    .sorted(((o1, o2) -> o1.getPublishTime().before(o2.getPublishTime()) ? 1 : -1))
                                            // 如果没满则取
                                    .limit(applicationSubmitList.size() < SIZE ? (shouldComment.get(item) != null ? shouldComment.get(item) : 1) : 0)
                                    .collect(Collectors.toList())));
                    if (applicationSubmitList.size() < SIZE) {
                        // level5
                        // 直接从本月的提交里取
                        List<Integer> profileIds = applicationSubmitList.stream().map(ApplicationSubmit::getProfileId).collect(Collectors.toList());
                        submitGroup.keySet()
                                .stream().filter(item -> !profileIds.contains(item))
                                .forEach(item -> applicationSubmitList.addAll(submitGroup.get(item)
                                        .stream()
                                                // 越晚提交被露出概率越高
                                        .sorted(((o1, o2) -> o1.getPublishTime().before(o2.getPublishTime()) ? 1 : -1))
                                                // 如果没满则取
                                        .limit(applicationSubmitList.size() < SIZE ? (shouldComment.get(item) != null ? shouldComment.get(item) : 1) : 0)
                                        .collect(Collectors.toList())));
                    }
                }
            }
        }

        List<ApplicationSubmit> applicationSubmits = applicationSubmitDao.batchLoadApplications(applicationSubmitList.stream().map(ApplicationSubmit::getId).collect(Collectors.toList()));
        applicationSubmitList.forEach(applicationSubmit -> {
            Optional<ApplicationSubmit> first = applicationSubmits.stream().filter(item -> item.getId() == applicationSubmit.getId()).findFirst();
            first.ifPresent(applicationSubmit1 -> applicationSubmit.setContent(applicationSubmit1.getContent()));
            RiseWorkInfoDto riseWorkInfoDto = buildApplicationSubmit(applicationSubmit);
            //设置应用练习题目
            applicationPractices.forEach(applicationPractice -> {
                if (applicationSubmit.getApplicationId().equals(applicationPractice.getId())) {
                    riseWorkInfoDto.setTitle(applicationPractice.getTopic());
                }
            });
            underCommentArticles.add(riseWorkInfoDto);
        });

        return underCommentArticles;
    }

    @Override
    public List<RiseWorkInfoDto> getUnderCommentApplicationsByNickName(Integer problemId, String nickName) {
        List<Profile> profiles = accountService.loadAllProfilesByNickName(nickName);
        if (profiles.size() == 0) {
            return Lists.newArrayList();
        }
        List<Integer> profileIds = profiles.stream().map(Profile::getId).collect(Collectors.toList());

        return getWorkInfoDtos(problemId, profileIds);
    }

    @Override
    public List<RiseWorkInfoDto> getUnderCommentApplicationsByMemberId(Integer problemId, String memberId) {
        List<RiseWorkInfoDto> workInfoDtos = Lists.newArrayList();
        RiseClassMember riseClassMember = riseClassMemberDao.queryByMemberId(memberId);
        if (riseClassMember != null && riseClassMember.getProfileId() != null) {
            Profile profile = accountService.getProfile(riseClassMember.getProfileId());
            if (profile != null) {
                List<ApplicationSubmit> submits = applicationSubmitDao.loadSubmitsByProfileId(problemId, profile.getId());
                submits.sort(Comparator.comparing(ApplicationSubmit::getPublishTime).reversed());

                List<ApplicationPractice> applicationPractices = applicationPracticeDao.getAllPracticeByProblemId(problemId);
                for (ApplicationSubmit submit : submits) {
                    RiseWorkInfoDto riseWorkInfoDto = buildApplicationSubmit(submit);
                    applicationPractices.forEach(applicationPractice -> {
                        if (submit.getApplicationId().equals(applicationPractice.getId())) {
                            riseWorkInfoDto.setTitle(applicationPractice.getTopic());
                        }
                    });
                    workInfoDtos.add(riseWorkInfoDto);
                }
            }
        }
        return workInfoDtos;
    }

    /**
     * 根据班级和小组查询所有待点评的应用题
     *
     * @param className
     * @param groupId
     * @return
     */
    @Override
    public List<RiseWorkInfoDto> getUnderCommentApplicationsByClassNameAndGroup(Integer problemId, String className, String groupId) {
        List<RiseClassMember> riseClassMembers = riseClassMemberDao.getRiseClassMemberByClassNameGroupId(className, groupId);
        List<Integer> profiles = riseClassMembers.stream().map(RiseClassMember::getProfileId).collect(Collectors.toList());

        return getWorkInfoDtos(problemId, profiles);
    }

    @Override
    public Map<Integer, Integer> getUnderCommentApplicationCount() {
        List<UnderCommentCount> underCommentCounts = applicationSubmitDao.getUnderCommentCount();
        Map<Integer, Integer> countMap = Maps.newHashMap();
        underCommentCounts.forEach(underCommentCount -> countMap.put(underCommentCount.getProblemId(), underCommentCount.getCount()));
        return countMap;
    }

    @Override
    public List<RiseWorkInfoDto> getCommentedSubmit(Integer profileId) {
        List<RiseWorkInfoDto> riseWorkInfoDtos = Lists.newArrayList();
        List<Comment> comments = commentDao.loadCommentsByProfileId(profileId);

        List<Integer> applicationSubmitIdsList = Lists.newArrayList();

        comments.forEach(comment -> {
            if (comment.getModuleId().equals(Constants.CommentModule.APPLICATION)) {
                applicationSubmitIdsList.add(comment.getReferencedId());
            }
        });

        List<ApplicationSubmit> applicationSubmitList = applicationSubmitDao.loadSubmits(applicationSubmitIdsList);

        //按照评论顺序,组装RiseWorkInfoDto
        comments.forEach(comment -> {
            if (comment.getModuleId().equals(Constants.CommentModule.APPLICATION)) {
                for (ApplicationSubmit applicationSubmit : applicationSubmitList) {
                    if (applicationSubmit.getId() == comment.getReferencedId()) {
                        RiseWorkInfoDto riseWorkInfoDto = buildApplicationSubmit(applicationSubmit);
                        riseWorkInfoDtos.add(riseWorkInfoDto);
                        break;
                    }
                }
            }
        });

        //过滤重复的文章
        Map<String, RiseWorkInfoDto> filterMap = Maps.newLinkedHashMap();
        riseWorkInfoDtos.forEach(riseWorkInfoDto -> filterMap.putIfAbsent(riseWorkInfoDto.getSubmitId().toString() + "-" + riseWorkInfoDto.getType().toString(), riseWorkInfoDto));

        return Lists.newArrayList(filterMap.values());
    }

    private RiseWorkInfoDto buildApplicationSubmit(ApplicationSubmit applicationSubmit) {
        RiseWorkInfoDto riseWorkInfoDto = new RiseWorkInfoDto(applicationSubmit);
        if (riseWorkInfoDto.getContent() != null) {
            riseWorkInfoDto.setContent(HtmlRegexpUtil.filterHtml(riseWorkInfoDto.getContent()));
            riseWorkInfoDto.setContent(riseWorkInfoDto.getContent().length() > 180 ?
                    riseWorkInfoDto.getContent().substring(0, 180) + "......" :
                    riseWorkInfoDto.getContent());
        }
        //设置用户信息
        buildRiseWorkInfo(riseWorkInfoDto, applicationSubmit.getProfileId());
        return riseWorkInfoDto;
    }

    private void buildRiseWorkInfo(RiseWorkInfoDto riseWorkInfoDto, Integer profileId) {
        Profile profile = accountService.getProfile(profileId);
        if (profile != null) {
            riseWorkInfoDto.setHeadPic(profile.getHeadimgurl());
            riseWorkInfoDto.setRole(profile.getRole());
            riseWorkInfoDto.setUpName(profile.getNickname());
            riseWorkInfoDto.setSignature(profile.getSignature());
        }
    }


    /**
     * 获得RiseClassMember中的ClassName和GroupId
     *
     * @return
     */
    @Override
    public List<RiseClassMember> loadClassNameAndGroupId() {
        return riseClassMemberDao.loadAllClassNameAndGroup();
    }

    private void initWorkInfoDto(ApplicationSubmit requestSubmit, List<ApplicationPractice> practices, List<RiseWorkInfoDto> workInfoDtos) {
        RiseWorkInfoDto riseWorkInfoDto = buildApplicationSubmit(requestSubmit);
        practices.forEach(applicationPractice -> {
            if (requestSubmit.getApplicationId().equals(applicationPractice.getId())) {
                riseWorkInfoDto.setTitle(applicationPractice.getTopic());
            }
        });
        workInfoDtos.add(riseWorkInfoDto);
    }


    /**
     * 返回200条应用提交（求点评在前，不需要点评在后，按照发布时间降序排序）
     *
     * @param problemId  小课id
     * @param profileIds 被查询的人
     * @return 结果列表
     */
    private List<RiseWorkInfoDto> getWorkInfoDtos(Integer problemId, List<Integer> profileIds) {
        List<RiseWorkInfoDto> workInfoDtos = Lists.newArrayList();
        if (profileIds.size() == 0) {
            return Lists.newArrayList();
        }
        // 加载求点评且未点评的人
        List<ApplicationSubmit> requestSubmits = applicationSubmitDao.loadRequestByProfileIds(problemId, profileIds);
        requestSubmits.sort(Comparator.comparing(ApplicationSubmit::getPublishTime).reversed());
        List<ApplicationPractice> applicationPractices = applicationPracticeDao.getAllPracticeByProblemId(problemId);
        if (requestSubmits.size() >= APPLICTION_SIZE) {
            requestSubmits
                    .subList(0, APPLICTION_SIZE)
                    .forEach(requestSubmit -> this.initWorkInfoDto(requestSubmit, applicationPractices, workInfoDtos));
            return workInfoDtos;
        } else {
            //将求点评的塞到返回值中
            requestSubmits.forEach(requestSubmit -> this.initWorkInfoDto(requestSubmit, applicationPractices, workInfoDtos));
            List<ApplicationSubmit> unRequestSubmits = applicationSubmitDao.loadUnRequestByProfileIds(problemId, profileIds);
            unRequestSubmits.sort(Comparator.comparing(ApplicationSubmit::getPublishTime).reversed());
            int reamin = APPLICTION_SIZE - workInfoDtos.size();
            if (unRequestSubmits.size() >= reamin) {
                unRequestSubmits.subList(0, reamin)
                        .forEach(unRequestSubmit -> this.initWorkInfoDto(unRequestSubmit, applicationPractices, workInfoDtos));
            } else {
                unRequestSubmits
                        .forEach(unRequestSubmit -> this.initWorkInfoDto(unRequestSubmit, applicationPractices, workInfoDtos));
            }
            return workInfoDtos;
        }
    }

    /**
     * 加载所有的教练
     *
     * @return 教练名单
     */
    @Override
    public List<UserRole> loadAssists() {
        return userRoleDao.loadAssists();
    }

    /**
     * 对教练进行升降级
     *
     * @param id     主键
     * @param roleId 角色id
     * @return 更新条数
     */
    @Override
    public Integer updateAssist(Integer id, Integer roleId) {
        return userRoleDao.updateAssist(id, roleId);
    }

    /**
     * 教练过期
     *
     * @param id 主键
     * @return 更新条数
     */
    @Override
    public Integer deleteAssist(Integer id) {
        return userRoleDao.deleteAssist(id);
    }

    /**
     * 根据昵称获取非助教信息
     *
     * @param nickName 昵称
     * @return 非助教列表
     */
    @Override
    public List<Profile> loadUnAssistByNickName(String nickName) {
        List<Profile> profiles = accountService.loadProfilesByNickName(nickName);
        if (profiles.size() == 0) {
            return profiles;
        }
        List<Profile> unAssistProfiles = Lists.newArrayList();
        profiles.forEach(profile -> {
            if (userRoleDao.loadAssist(profile.getId()) == null) {
                unAssistProfiles.add(profile);
            }
        });
        return unAssistProfiles;
    }

    /**
     * 添加教练
     *
     * @param roleId 角色id
     * @param riseId 圈外id
     * @return 添加结果
     */
    @Override
    public Integer addAssist(Integer roleId, String riseId) {
        Profile profile = accountService.getProfileByRiseId(riseId);
        if (profile == null) {
            return -1;
        }
        //判断是否已经是教练
        if (userRoleDao.loadAssist(profile.getId()) != null) {
            return -1;
        }
        return userRoleDao.insertAssist(roleId, profile.getId());
    }

    @Override
    public List<BusinessSchoolApplication> loadByInterviewer(Integer interviewer, Page page) {
        page.setTotal(businessSchoolApplicationDao.loadAssistBACount(interviewer));
        return businessSchoolApplicationDao.loadByInterviewer(interviewer, page);
    }

    @Override
    public InterviewRecord loadInterviewRecord(Integer applyId) {
        return interviewRecordDao.queryByApplyId(applyId);
    }

    @Override
    public Integer addInterviewRecord(InterviewRecord interviewRecord) {
        Integer applyId = interviewRecord.getApplyId();
        InterviewRecord existInterviewRecord = interviewRecordDao.queryByApplyId(applyId);
        if (existInterviewRecord == null) {
            return interviewRecordDao.insert(interviewRecord);
        } else {
            interviewRecord.setId(existInterviewRecord.getId());
            //判断是管理员还是助教
            if(interviewRecord.getApprovalId()!=null){
                return interviewRecordDao.updateByAdmin(interviewRecord);
            }
            return interviewRecordDao.updateByAssist(interviewRecord);
        }
    }

    @Override
    public Integer loadAssignedCount(Integer interviewer) {
        return businessSchoolApplicationDao.loadAssistBACount(interviewer);
    }
}
