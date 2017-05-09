package com.iquanwai.confucius.biz.domain.asst;

import com.iquanwai.confucius.biz.domain.fragmentation.practice.RiseWorkInfoDto;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 17/4/26.
 */
public interface AssistantCoachService {

    /**
     * 获取助教的评论数
     * @param openid 助教openid
     * @return 当日评论数/总评论数
     * */
    Pair<Integer, Integer> getCommentCount(String openid);


    /**
     * 获取待评论的小课分享
     * @param problemId 小课id
     * */
    List<RiseWorkInfoDto> getUnderCommentArticles(Integer problemId);


    /**
     * 获取待评论的应用练习
     * @param problemId 小课id
     * */
    List<RiseWorkInfoDto> getUnderCommentApplications(Integer problemId);

    /**
     * 获取待评论的应用练习数量
     * */
    Map<Integer, Integer> getUnderCommentApplicationCount();

    /**
     * 获取待评论的小课分享数量
     * */
    Map<Integer, Integer> getUnderCommentSubjectArticleCount();
}
