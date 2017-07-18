package com.iquanwai.confucius.biz.dao;

import com.google.gson.GsonBuilder;
import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.dao.course.ClassDao;
import com.iquanwai.confucius.biz.dao.course.ClassMemberDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseCourseOrderDao;
import com.iquanwai.confucius.biz.po.systematism.QuanwaiClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Created by justin on 16/8/29.
 */

public class ClassDaoTest extends TestBase {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ClassDao classDao;
    @Autowired
    private ClassMemberDao classMemberDao;
    @Autowired
    private RiseCourseOrderDao riseCourseOrderDao;

    @Test
    public void load(){
        QuanwaiClass cla = classDao.load(QuanwaiClass.class, 1);
    }

    @Test
    public void loadOrder(){
        System.out.print(riseCourseOrderDao.loadOrder("dx2cii8dar0i28rh"));
    }
    @Test
    public void testJson(){
        QuanwaiClass cla = classDao.load(QuanwaiClass.class, 1);
        System.out.println(new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create().toJson(cla));
    }
}
