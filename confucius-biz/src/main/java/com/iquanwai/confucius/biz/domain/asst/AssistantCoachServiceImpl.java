package com.iquanwai.confucius.biz.domain.asst;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.fragmentation.ApplicationPracticeDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ApplicationSubmitDao;
import com.iquanwai.confucius.biz.dao.fragmentation.CommentDao;
import com.iquanwai.confucius.biz.dao.fragmentation.SubjectArticleDao;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.RiseWorkInfoDto;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationPractice;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.Comment;
import com.iquanwai.confucius.biz.po.fragmentation.SubjectArticle;
import com.iquanwai.confucius.biz.util.DateUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
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
        List<SubjectArticle> subjectArticles = subjectArticleDao.loadUnderCommentArticles(problemId, SIZE, date);
        subjectArticles.stream().forEach(subjectArticle ->{
            RiseWorkInfoDto riseWorkInfoDto = new RiseWorkInfoDto(subjectArticle);
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
        List<Integer> applicationPracticeIds = applicationPractices.stream().map(ApplicationPractice::getId).collect(Collectors.toList());
        //只评论30天内的文章
        Date date = DateUtils.beforeDays(new Date(), PREVIOUS_DAY);

        List<ApplicationSubmit> applicationSubmitList = applicationSubmitDao.getSubmitByApplicationIds(applicationPracticeIds, SIZE, date);
        applicationSubmitList.stream().forEach(applicationSubmit ->{
            RiseWorkInfoDto riseWorkInfoDto = new RiseWorkInfoDto(applicationSubmit);
            //设置应用练习题目
            applicationPractices.stream().forEach(applicationPractice -> {
                if(applicationSubmit.getApplicationId().equals(applicationPractice.getId())){
                    riseWorkInfoDto.setTitle(applicationPractice.getTopic());
                }
            });
            //设置用户信息
            buildRiseWorkInfo(riseWorkInfoDto, applicationSubmit.getOpenid());
            underCommentArticles.add(riseWorkInfoDto);
        });

        return underCommentArticles;
    }

    private void buildRiseWorkInfo(RiseWorkInfoDto riseWorkInfoDto, String openid){
        Profile profile = accountService.getProfile(openid, false);
        riseWorkInfoDto.setHeadPic(profile.getHeadimgurl());
        riseWorkInfoDto.setRole(profile.getRole());
        riseWorkInfoDto.setUpName(profile.getNickname());
        riseWorkInfoDto.setSignature(profile.getSignature());
    }

}
