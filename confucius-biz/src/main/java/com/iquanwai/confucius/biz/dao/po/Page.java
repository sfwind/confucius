package com.iquanwai.confucius.biz.dao.po;

import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.util.List;

/**
 * Created by justin on 16/8/25.
 */
@Data
@Alias("page")
public class Page {
    private int id;
    private Integer chapterId; //章节id
    private Integer sequence;  //章节内顺序
    private List<Material> materialList;
}
