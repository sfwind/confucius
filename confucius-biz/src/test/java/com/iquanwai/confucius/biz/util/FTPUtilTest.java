package com.iquanwai.confucius.biz.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by 三十文 on 2017/9/22
 */
public class FTPUtilTest {


    public static void main(String[] args) {
        String host = "139.196.45.104";
        String login = "ftpuser";
        String password = "QaDLxQ19X7xtG0jw";
        FTPUtil ftpUtil = new FTPUtil();
        try {
            boolean successConnect = ftpUtil.connect(host, login, password, 21);
            System.out.println("successConnect = " + successConnect);
            System.out.println(successConnect ? "成功登陆" : "登陆失败");
            File file = new File("/Users/xfduan/Pictures/test.jpg");
            InputStream stream = new FileInputStream(new File("/Users/xfduan/Pictures/test.jpg"));
            ftpUtil.storeFile("/data/static/images/test/test.jpg", stream);

            // ftpUtil.downloadFile("/data/static/images/test/testsvg.svg", "/Users/xfduan/Pictures/hohoo.svg");
            System.out.println("文件上传成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
