package com.iquanwai.confucius.biz.service;

import com.google.common.collect.Lists;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static java.lang.System.currentTimeMillis;

public class ImageTest {

    public static void main(String[] args) {
        List<String> urls = Lists.newArrayList();
        urls.add("http://wx.qlogo.cn/mmopen/ajNVdqHZLLDhFic8Wpv65HD2QZRnelbibUOmkV5diaPdmtVU0oXTh1sdicCjflPH3SauiaxVqmCBiaaX0emBgKgak35w/0");
        urls.add("http://wx.qlogo.cn/mmopen/wbKdib81ny6ibmlxQPHfibYXJJCBGVJRHBeVMoxM2TiamqQJ755xUBXB0n4p8claKHHZVwcfviaiaiaiaN4h2kYyoq2Ppw/0");
        Long startTime = currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            Request request = new Request.Builder()
                    .url(urls.get(new Random().nextInt(2)))
                    .get()
                    .build();
            OkHttpClient client = new OkHttpClient();
            try {
                Response response = client.newCall(request).execute();
                String errorNo = response.header("X-ErrNo");
                if (errorNo != null) {
                    System.out.println(errorNo);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Long endTime = currentTimeMillis();
        System.out.println("总时间" + (endTime - startTime));
    }

}


