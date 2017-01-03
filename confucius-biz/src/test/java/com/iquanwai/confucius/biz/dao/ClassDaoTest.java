package com.iquanwai.confucius.biz.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.dao.course.ClassDao;
import com.iquanwai.confucius.biz.po.QuanwaiClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 16/8/29.
 */

public class ClassDaoTest extends TestBase {
    @Autowired
    private ClassDao classDao;

    @Test
    public void load(){
        QuanwaiClass cla = classDao.load(QuanwaiClass.class, 1);
    }

    @Test
    public void testJson(){
        QuanwaiClass cla = classDao.load(QuanwaiClass.class, 1);
        System.out.println(new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create().toJson(cla));
    }
}
