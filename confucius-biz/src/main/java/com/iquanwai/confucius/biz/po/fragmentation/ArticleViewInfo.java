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
    private Integer articleType;
    private Integer articleId;
    private Integer count;

    public ArticleViewInfo(Integer articleType,Integer articleId){
        this.articleType = articleType;
        this.articleId = articleId;
    }
}
