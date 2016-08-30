package com.iquanwai.confucius.biz;

import com.iquanwai.confucius.biz.dao.ClassDao;
import com.iquanwai.confucius.biz.dao.po.QuanwaiClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 16/8/29.
 */

public class ClassDaoTest extends TestBase{
    @Autowired
    private ClassDao classDao;

    @Test
    public void load(){
        QuanwaiClass cla = classDao.load(QuanwaiClass.class, 1);
    }
}
