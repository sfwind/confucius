package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;
import lombok.NoArgsConstructor;

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
}
