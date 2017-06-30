package com.iquanwai.confucius.biz.domain.asst;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.fragmentation.*;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.RiseWorkInfoDto;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.HtmlRegexpUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 17/4/26.
 */
@Service
public class AssistantCoachServiceImpl implements AssistantCoachService {
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private ApplicationSubmitDao applicationSubmitDao;
    @Autowired
    private SubjectArticleDao subjectArticleDao;
    @Autowired
    private ApplicationPracticeDao applicationPracticeDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AsstCoachCommentDao asstCoachCommentDao;
    @Autowired
    private RiseMemberDao riseMemberDao;

    private static final int SIZE = 50;

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
            if (filterMap.get(comment.getReferencedId().toString() + "-" + comment.getModuleId().toString()) == null) {
                filterMap.put(comment.getReferencedId().toString() + "-" + comment.getModuleId().toString(), comment);
            }
        });
        Long todayCommentCnt = Lists.newArrayList(filterMap.values()).stream().filter(comment -> DateUtils.isToday(comment.getAddTime()))
                .distinct().count();

        return new ImmutablePair<>(todayCommentCnt.intValue(), totalCommentCnt.intValue());
    }

    @Override
    public List<RiseWorkInfoDto> getUnderCommentArticles(Integer problemId) {
        List<RiseWorkInfoDto> underCommentArticles = Lists.newArrayList();
        //只评论30天内的文章
        Date date = DateUtils.beforeDays(new Date(), PREVIOUS_DAY);
        int size = SIZE;
        //获取求点评的文章
        List<SubjectArticle> subjectArticles = Lists.newArrayList();
        List<SubjectArticle> list = subjectArticleDao.loadRequestCommentArticles(problemId, size);
        subjectArticles.addAll(list);
        size = size - list.size();

        if (size > 0) {
            //已评价用户id
            List<Integer> profileIds = asstCoachCommentDao.loadCommentedStudent(problemId).stream()
                    .map(AsstCoachComment::getProfileId).collect(Collectors.toList());
            //精英用户id
            List<Integer> elites = riseMemberDao.eliteMembers().stream()
                    .map(RiseMember::getProfileId).collect(Collectors.toList());

            //未点评精英=精英-已点评用户
            List<Integer> unCommentedElite = Lists.newArrayList(elites);
            unCommentedElite.removeAll(profileIds);
            list = subjectArticleDao.loadUnderCommentArticlesIncludeSomeone(problemId, size, date, unCommentedElite);
            subjectArticles.addAll(list);
            size = size - list.size();
            if (size > 0) {
                //未点评普通=所有-（精英+已点评用户)
                List<Integer> unCommentedNormal = Lists.newArrayList(elites);
                unCommentedNormal.addAll(profileIds);
                list = subjectArticleDao.loadUnderCommentArticlesExcludeSomeone(problemId, size, date, unCommentedNormal);
                subjectArticles.addAll(list);
                size = size - list.size();
                if (size > 0) {
                    //已点评精英=精英&已点评用户
                    List<Integer> commentedElite = Lists.newArrayList(elites);
                    commentedElite.retainAll(profileIds);
                    list = subjectArticleDao.loadUnderCommentArticlesIncludeSomeone(problemId, size, date, commentedElite);
                    subjectArticles.addAll(list);
                    size = size - list.size();
                    if (size > 0) {
                        //已点评精英=已点评用户-精英
                        List<Integer> commentedNormal = Lists.newArrayList(profileIds);
                        commentedNormal.removeAll(elites);
                        list = subjectArticleDao.loadUnderCommentArticlesIncludeSomeone(problemId, size, date, commentedNormal);
                        subjectArticles.addAll(list);
                    }
                }
            }
        }

        subjectArticles.stream().forEach(subjectArticle -> {
            RiseWorkInfoDto riseWorkInfoDto = buildSubjectArticle(subjectArticle);
            underCommentArticles.add(riseWorkInfoDto);
        });
        return underCommentArticles;
    }

    @Override
    public List<RiseWorkInfoDto> getUnderCommentApplications(Integer problemId) {
        List<RiseWorkInfoDto> underCommentArticles = Lists.newArrayList();
        //找出小课的所有应用练习包括删除的
        List<ApplicationPractice> applicationPractices = applicationPracticeDao.getAllPracticeByProblemId(problemId);
        //只评论30天内的文章
        Date date = DateUtils.beforeDays(new Date(), PREVIOUS_DAY);

        int size = SIZE;
        //获取求点评的文章
        List<ApplicationSubmit> applicationSubmitList = Lists.newArrayList();
        List<ApplicationSubmit> list = applicationSubmitDao.loadRequestCommentApplications(problemId, size);
        applicationSubmitList.addAll(list);
        size = size - list.size();

        if (size > 0) {
            //已评价用户id
            List<Integer> profileIds = asstCoachCommentDao.loadCommentedStudent(problemId).stream()
                    .map(AsstCoachComment::getProfileId).collect(Collectors.toList());
            //精英用户id
            List<Integer> elites = riseMemberDao.eliteMembers().stream()
                    .map(RiseMember::getProfileId).collect(Collectors.toList());

            //未点评精英=精英-已点评用户
            List<Integer> unCommentedElite = Lists.newArrayList(elites);
            unCommentedElite.removeAll(profileIds);
            list = applicationSubmitDao.loadUnderCommentApplicationsIncludeSomeone(problemId, size, date, unCommentedElite);
            applicationSubmitList.addAll(list);
            size = size - list.size();
            if (size > 0) {
                //未点评普通=所有-（精英+已点评用户)
                List<Integer> unCommentedNormal = Lists.newArrayList(elites);
                unCommentedNormal.addAll(profileIds);
                list = applicationSubmitDao.loadUnderCommentApplicationsExcludeSomeone(problemId, size, date, unCommentedNormal);
                applicationSubmitList.addAll(list);
                size = size - list.size();
                if (size > 0) {
                    //已点评精英=精英&已点评用户
                    List<Integer> commentedElite = Lists.newArrayList(elites);
                    commentedElite.retainAll(profileIds);
                    list = applicationSubmitDao.loadUnderCommentApplicationsIncludeSomeone(problemId, size, date, commentedElite);
                    applicationSubmitList.addAll(list);
                    size = size - list.size();
                    if (size > 0) {
                        //已点评精英=已点评用户-精英
                        List<Integer> commentedNormal = Lists.newArrayList(profileIds);
                        commentedNormal.removeAll(elites);
                        list = applicationSubmitDao.loadUnderCommentApplicationsIncludeSomeone(problemId, size, date, commentedNormal);
                        applicationSubmitList.addAll(list);
                    }
                }
            }
        }

        applicationSubmitList.stream().forEach(applicationSubmit -> {
            RiseWorkInfoDto riseWorkInfoDto = buildApplicationSubmit(applicationSubmit);
            //设置应用练习题目
            applicationPractices.stream().forEach(applicationPractice -> {
                if (applicationSubmit.getApplicationId().equals(applicationPractice.getId())) {
                    riseWorkInfoDto.setTitle(applicationPractice.getTopic());
                }
            });
            underCommentArticles.add(riseWorkInfoDto);
        });

        return underCommentArticles;
    }

    @Override
    public Map<Integer, Integer> getUnderCommentApplicationCount() {
        List<UnderCommentCount> underCommentCounts = applicationSubmitDao.getUnderCommentCount();
        Map<Integer, Integer> countMap = Maps.newHashMap();
        underCommentCounts.stream().forEach(underCommentCount -> {
            countMap.put(underCommentCount.getProblemId(), underCommentCount.getCount());
        });
        return countMap;
    }

    @Override
    public Map<Integer, Integer> getUnderCommentSubjectArticleCount() {
        List<UnderCommentCount> underCommentCounts = subjectArticleDao.getUnderCommentCount();
        Map<Integer, Integer> countMap = Maps.newHashMap();
        underCommentCounts.stream().forEach(underCommentCount -> {
            countMap.put(underCommentCount.getProblemId(), underCommentCount.getCount());
        });
        return countMap;
    }

    @Override
    public List<RiseWorkInfoDto> getCommentedSubmit(Integer profileId) {
        List<RiseWorkInfoDto> riseWorkInfoDtos = Lists.newArrayList();
        List<Comment> comments = commentDao.loadCommentsByProfileId(profileId);

        List<Integer> subjectArticleIdsList = Lists.newArrayList();
        List<Integer> applicationSubmitIdsList = Lists.newArrayList();

        comments.stream().forEach(comment -> {
            if (comment.getModuleId().equals(Constants.CommentModule.APPLICATION)) {
                applicationSubmitIdsList.add(comment.getReferencedId());
            } else if (comment.getModuleId().equals(Constants.CommentModule.SUBJECT)) {
                subjectArticleIdsList.add(comment.getReferencedId());
            }
        });

        List<SubjectArticle> subjectArticleList = subjectArticleDao.loadArticles(subjectArticleIdsList);
        List<ApplicationSubmit> applicationSubmitList = applicationSubmitDao.loadSubmits(applicationSubmitIdsList);

        //按照评论顺序,组装RiseWorkInfoDto
        comments.stream().forEach(comment -> {
            if (comment.getModuleId().equals(Constants.CommentModule.SUBJECT)) {
                for (SubjectArticle subjectArticle : subjectArticleList) {
                    if (subjectArticle.getId().equals(comment.getReferencedId())) {
                        RiseWorkInfoDto riseWorkInfoDto = buildSubjectArticle(subjectArticle);
                        riseWorkInfoDtos.add(riseWorkInfoDto);
                        break;
                    }
                }
            } else if (comment.getModuleId().equals(Constants.CommentModule.APPLICATION)) {
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
        riseWorkInfoDtos.forEach(riseWorkInfoDto -> {
            if (filterMap.get(riseWorkInfoDto.getSubmitId().toString() + "-" + riseWorkInfoDto.getType().toString()) == null) {
                filterMap.put(riseWorkInfoDto.getSubmitId().toString() + "-" + riseWorkInfoDto.getType().toString(), riseWorkInfoDto);
            }
        });

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

    private RiseWorkInfoDto buildSubjectArticle(SubjectArticle subjectArticle) {
        RiseWorkInfoDto riseWorkInfoDto = new RiseWorkInfoDto(subjectArticle);
        if (riseWorkInfoDto.getContent() != null) {
            riseWorkInfoDto.setContent(HtmlRegexpUtil.filterHtml(riseWorkInfoDto.getContent()));
            riseWorkInfoDto.setContent(riseWorkInfoDto.getContent().length() > 180 ?
                    riseWorkInfoDto.getContent().substring(0, 180) + "......" :
                    riseWorkInfoDto.getContent());
        }
        //设置用户信息
        buildRiseWorkInfo(riseWorkInfoDto, subjectArticle.getProfileId());

        return riseWorkInfoDto;
    }

}
