package com.iquanwai.confucius.biz.domain.asst;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.fragmentation.*;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.RiseWorkInfoDto;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.HtmlRegexpUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public Pair<Integer, Integer> getCommentCount(String openid) {
        List<Comment> commentList = commentDao.loadCommentsByOpenid(openid);
        List<Comment> todayComments = commentList.stream().filter(comment -> DateUtils.interval(comment.getAddTime())==0)
                .collect(Collectors.toList());
        return new ImmutablePair<>(todayComments.size(), commentList.size());
    }

    @Override
    public List<RiseWorkInfoDto> getUnderCommentArticles(Integer problemId) {
        List<RiseWorkInfoDto> underCommentArticles = Lists.newArrayList();
        //只评论30天内的文章
        Date date = DateUtils.beforeDays(new Date(), PREVIOUS_DAY);
        int size = SIZE;
        //获取求点评的文章
        List<SubjectArticle> subjectArticles = Lists.newArrayList();
        List<SubjectArticle> list = subjectArticleDao.loadRequestCommentArticles(problemId, size, date);
        subjectArticles.addAll(list);
        size = size - list.size();

        if(size>0){
            //已评价用户openid
            List<String> openIds = asstCoachCommentDao.loadCommentedStudent(problemId).stream()
                    .map(AsstCoachComment::getOpenid).collect(Collectors.toList());
            //精英用户openid
            List<String> elites = riseMemberDao.eliteMembers().stream()
                    .map(RiseMember::getOpenId).collect(Collectors.toList());

            //未点评精英=精英-已点评用户
            List<String> unCommentedElite = Lists.newArrayList(elites);
            unCommentedElite.removeAll(openIds);
            list = subjectArticleDao.loadUnderCommentArticlesIncludeSomeone(problemId, size, date, unCommentedElite);
            subjectArticles.addAll(list);
            size = size - list.size();
            if(size>0){
                //未点评普通=所有-（精英+已点评用户)
                List<String> unCommentedNormal = Lists.newArrayList(elites);
                unCommentedNormal.addAll(openIds);
                list = subjectArticleDao.loadUnderCommentArticlesExcludeSomeone(problemId, size, date, unCommentedNormal);
                subjectArticles.addAll(list);
                size = size - list.size();
                if(size>0){
                    //已点评精英=精英&已点评用户
                    List<String> commentedElite = Lists.newArrayList(elites);
                    commentedElite.retainAll(openIds);
                    list = subjectArticleDao.loadUnderCommentArticlesIncludeSomeone(problemId, size, date, commentedElite);
                    subjectArticles.addAll(list);
                    size = size - list.size();
                    if(size>0){
                        //已点评精英=已点评用户-精英
                        List<String> commentedNormal = Lists.newArrayList(openIds);
                        commentedNormal.removeAll(elites);
                        list = subjectArticleDao.loadUnderCommentArticlesIncludeSomeone(problemId, size, date, commentedNormal);
                        subjectArticles.addAll(list);
                    }
                }
            }
        }

        subjectArticles.stream().forEach(subjectArticle ->{
            RiseWorkInfoDto riseWorkInfoDto = new RiseWorkInfoDto(subjectArticle);
            if(riseWorkInfoDto.getContent()!=null) {
                riseWorkInfoDto.setContent(HtmlRegexpUtil.filterHtml(riseWorkInfoDto.getContent()));
                riseWorkInfoDto.setContent(riseWorkInfoDto.getContent().length() > 180 ?
                        riseWorkInfoDto.getContent().substring(0, 180) + "......" :
                        riseWorkInfoDto.getContent());
            }
            //设置用户信息
            buildRiseWorkInfo(riseWorkInfoDto, subjectArticle.getOpenid());
            underCommentArticles.add(riseWorkInfoDto);
        });
        return underCommentArticles;
    }

    @Override
    public List<RiseWorkInfoDto> getUnderCommentApplications(Integer problemId) {
        List<RiseWorkInfoDto> underCommentArticles = Lists.newArrayList();
        //找出小课的所有应用练习
        List<ApplicationPractice> applicationPractices = applicationPracticeDao.getPracticeByProblemId(problemId);
        //只评论30天内的文章
        Date date = DateUtils.beforeDays(new Date(), PREVIOUS_DAY);

        int size = SIZE;
        //获取求点评的文章
        List<ApplicationSubmit> applicationSubmitList = Lists.newArrayList();
        List<ApplicationSubmit> list = applicationSubmitDao.loadRequestCommentApplications(problemId, size, date);
        applicationSubmitList.addAll(list);
        size = size - list.size();

        if(size>0){
            //已评价用户openid
            List<String> openIds = asstCoachCommentDao.loadCommentedStudent(problemId).stream()
                    .map(AsstCoachComment::getOpenid).collect(Collectors.toList());
            //精英用户openid
            List<String> elites = riseMemberDao.eliteMembers().stream()
                    .map(RiseMember::getOpenId).collect(Collectors.toList());

            //未点评精英=精英-已点评用户
            List<String> unCommentedElite = Lists.newArrayList(elites);
            unCommentedElite.removeAll(openIds);
            list = applicationSubmitDao.loadUnderCommentApplicationsIncludeSomeone(problemId, size, date, unCommentedElite);
            applicationSubmitList.addAll(list);
            size = size - list.size();
            if(size>0){
                //未点评普通=所有-（精英+已点评用户)
                List<String> unCommentedNormal = Lists.newArrayList(elites);
                unCommentedNormal.addAll(openIds);
                list = applicationSubmitDao.loadUnderCommentApplicationsExcludeSomeone(problemId, size, date, unCommentedNormal);
                applicationSubmitList.addAll(list);
                size = size - list.size();
                if(size>0){
                    //已点评精英=精英&已点评用户
                    List<String> commentedElite = Lists.newArrayList(elites);
                    commentedElite.retainAll(openIds);
                    list = applicationSubmitDao.loadUnderCommentApplicationsIncludeSomeone(problemId, size, date, commentedElite);
                    applicationSubmitList.addAll(list);
                    size = size - list.size();
                    if(size>0){
                        //已点评精英=已点评用户-精英
                        List<String> commentedNormal = Lists.newArrayList(openIds);
                        commentedNormal.removeAll(elites);
                        list = applicationSubmitDao.loadUnderCommentApplicationsIncludeSomeone(problemId, size, date, commentedNormal);
                        applicationSubmitList.addAll(list);
                    }
                }
            }
        }

        applicationSubmitList.stream().forEach(applicationSubmit -> {
            RiseWorkInfoDto riseWorkInfoDto = new RiseWorkInfoDto(applicationSubmit);
            if (riseWorkInfoDto.getContent() != null) {
                riseWorkInfoDto.setContent(HtmlRegexpUtil.filterHtml(riseWorkInfoDto.getContent()));
                riseWorkInfoDto.setContent(riseWorkInfoDto.getContent().length() > 180 ?
                        riseWorkInfoDto.getContent().substring(0, 180) + "......" :
                        riseWorkInfoDto.getContent());
            }
            //设置应用练习题目
            applicationPractices.stream().forEach(applicationPractice -> {
                if (applicationSubmit.getApplicationId().equals(applicationPractice.getId())) {
                    riseWorkInfoDto.setTitle(applicationPractice.getTopic());
                }
            });
            //设置用户信息
            buildRiseWorkInfo(riseWorkInfoDto, applicationSubmit.getOpenid());
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

    private void buildRiseWorkInfo(RiseWorkInfoDto riseWorkInfoDto, String openid){
        Profile profile = accountService.getProfile(openid, false);
        if(profile!=null){
            riseWorkInfoDto.setHeadPic(profile.getHeadimgurl());
            riseWorkInfoDto.setRole(profile.getRole());
            riseWorkInfoDto.setUpName(profile.getNickname());
            riseWorkInfoDto.setSignature(profile.getSignature());
        }
    }

}
