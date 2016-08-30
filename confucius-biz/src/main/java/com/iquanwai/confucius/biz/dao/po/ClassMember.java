package com.iquanwai.confucius.biz.dao.po;

import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * Created by justin on 16/8/29.
 */
@Data
@Alias("classMember")
public class ClassMember {
    private int id;
    private String openId;  //openid
    private Integer classId; //班级id
    private Boolean graduate; //是否毕业（0-否，1-是）
    private Integer score;    //课程积分
    private Integer level;  //课程等级，和勋章挂钩
    private Boolean superb; //是否优秀学员（1-是，0-否
    private Integer progress; //学员进度，章节的id
    private Integer progressWeek; //学员进度周
    private Boolean pass;  //是否通过课程（0-否，1-是）
    private Integer classProgress; //课程进度，章节的id
    private Integer courseId; //课程id

}
