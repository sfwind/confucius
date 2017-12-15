package com.iquanwai.confucius.biz.util;

import com.iquanwai.confucius.biz.TestBase;
import org.junit.Test;

import java.util.Date;

/**
 * Created by nethunder on 2017/1/17.
 */
public class ConfigUtilsTest extends TestBase {

    @Test
    public void testTime(){
        System.out.println(ConfigUtils.getRisePayStopTime());
        System.out.println(ConfigUtils.getRisePayStopTime().after(new Date()));
    }

//    线上课程生成,紧急修复代码
//    public static void main(String[] args) {
//         MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//
//        String cookie = "_act" + "=" + "AdwXJ5sSIsCv5XDsmIjAnBNEQAScVToa8yZXqltmAj0zDye_dvEuN-eFlPJkBuoNwVD9Dq6316tUO5YeXTkNiY08Q5q45OdkXVEixPO3ENo";
//        String url = "https://www.iquanwai.com/rise/plan/choose/problem/14";
//        if (StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty("{\"default\":\"null\"}")) {
//            Request request = new Request.Builder()
//                    .url(url)
//                    .post(RequestBody.create(JSON, "{\"default\":\"null\"}"))
//                    .addHeader("Cookie", cookie)
//                    .build();
//
//            try {
//                Response response = client.newCall(request).execute();
//                System.out.println(response.body().string());;
//            } catch (Exception e) {
//                System.out.println("execute " + url + " error");
//            }
//        }
//    }
}
