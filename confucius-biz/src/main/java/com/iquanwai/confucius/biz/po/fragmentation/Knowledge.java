package com.iquanwai.confucius.biz.po.fragmentation;

import com.iquanwai.confucius.biz.util.ConfigUtils;
import lombok.Data;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class Knowledge {
    private int id;
    private String knowledge; //知识点
    private Integer type; //知识点分类
    private String analysis; //作用
    private String means; //方法
    private String keynote; //要点
    private String analysisPic;// 作用图片
    private String meansPic;// 方法图片
    private String keynotePic;// 要点图片
    private String pic; //图片链接
    private String audio; //语音链接
    private Integer appear; //非db字段,是否出现过

    private static String REVIEW_KNOWLEDGE = ConfigUtils.getIntegratedPracticeIndex();

    public static boolean isReview(Integer knowledgeId){
        if(knowledgeId==null){
            return false;
        }
        String[] ids = REVIEW_KNOWLEDGE.split(",");
        for(String id:ids){
            if(id.equals(knowledgeId.toString())){
                return true;
            }
        }

        return false;
    }
}
