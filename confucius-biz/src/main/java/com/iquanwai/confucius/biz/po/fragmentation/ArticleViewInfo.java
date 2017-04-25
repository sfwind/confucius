package com.iquanwai.confucius.biz.po.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.util.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by nethunder on 2017/3/5.
 */
@Data
@NoArgsConstructor
public class ArticleViewInfo {
    private Integer id;
    private Integer articleModule;
    private Integer viewEventType;
    private Integer articleId;
    private Integer count;

    public ArticleViewInfo(Integer articleModule,Integer articleId,Integer viewEventType){
        this.articleModule = articleModule;
        this.viewEventType = viewEventType;
        this.articleId = articleId;
    }

    public static List<ArticleViewInfo> initArticleViews(Integer articleModule, Integer articleId) {
        List<ArticleViewInfo> list = Lists.newArrayList();
        list.add(new ArticleViewInfo(articleModule, articleId, Constants.ViewInfo.EventType.PC_SUBMIT));
        list.add(new ArticleViewInfo(articleModule, articleId, Constants.ViewInfo.EventType.MOBILE_SUBMIT));
        list.add(new ArticleViewInfo(articleModule, articleId, Constants.ViewInfo.EventType.PC_SHOW));
        list.add(new ArticleViewInfo(articleModule, articleId, Constants.ViewInfo.EventType.MOBILE_SHOW));
        return list;
    }
}
