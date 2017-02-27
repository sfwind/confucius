package com.iquanwai.confucius.biz.po.permisson;

import lombok.Data;

/**
 * Created by nethunder on 2016/12/28.
 */
@Data
public class Role {
    private Integer id;
    private String name;
    private Integer level;

    public static final Integer STRANGE = 0; //陌生人
    public static final Integer STUDENT = 1; //学生
    public static final Integer COACH_LEVEL_1 = 10; //见习教练
    public static final Integer COACH_LEVEL_2 = 20; //教练
    public static final Integer COACH_LEVEL_3 = 30; //高级教练
    public static final Integer COACH_LEVEL_4 = 40; //首席教练
    public static final Integer BIG_V = 18; //大V
    public static final Integer EMPLOYEE = 19; //员工
    public static final Integer CONTENT_ADMIN = 100; //内容管理员
    public static final Integer ADMIN = 1000; //管理员

    public static Role stranger(){
        Role stranger = new Role();
        stranger.setName("陌生人");
        stranger.setLevel(STRANGE);
        return stranger;
    }

    public static Role student(){
        Role stranger = new Role();
        stranger.setName("学生");
        stranger.setLevel(STUDENT);
        return stranger;
    }
}
