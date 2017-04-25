package com.iquanwai.confucius.biz.util;

import com.iquanwai.confucius.biz.TestBase;
import org.junit.Test;

import java.util.Date;

/**
 * Created by nethunder on 2017/1/17.
 */
public class ConfigUtilsTest extends TestBase {
    @Test
    public void testObjectConfig(){
        String obj = ConfigUtils.getSurveyUrl(1);
        System.out.println(obj);
        String obj1 = ConfigUtils.getSurveyUrl(2);
        System.out.println(obj1);
        String obj2 = ConfigUtils.getSurveyUrl(3);
        System.out.println(obj2);
        String obj3 = ConfigUtils.getSurveyUrl(11769325);
        System.out.println(obj3);
    }

    @Test
    public void testTime(){
        System.out.println(ConfigUtils.getRisePayStopTime());
        System.out.println(ConfigUtils.getRisePayStopTime().after(new Date()));
    }
}
