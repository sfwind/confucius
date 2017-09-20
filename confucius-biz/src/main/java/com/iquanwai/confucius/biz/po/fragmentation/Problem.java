package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;


/**
 * Created by justin on 16/12/4.
 */
@Data
public class Problem {
    private int id;
    private String problem; // 工作生活中遇到的问题
    private Integer length; // 训练节数
    private Integer catalogId; // 分类
    private Integer subCatalogId; // 子目录分类
    private String author = "孙圈圈"; // 讲师
    private String authorPic = "https://static.iqycamp.com/images/rise_problem_author_pic_sunquanquanV2.png?imageslim"; // 讲师图片
    private Double difficultyScore = 3.0d;// 难度
    private Double usefulScore = 4.0d; // 实用度
    private String descPic; // 描述图片
    private Integer audioId;
    private String who; //适合人群
    private String how; //如何学习
    private String why; //为什么学习
    private Boolean del; //是否删除(0-否,1-是)
    private Boolean trial = true; //试用版（0-否,1-是）
    private String abbreviation; // 缩略名

}
