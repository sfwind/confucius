package com.iquanwai.confucius.biz.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by yangyuchen on 15-1-30.
 */
public class SHA1Helper {
    public static String getSHA1String(String s) {
        //SHA-1加密实例
        MessageDigest sha1 = null;
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        sha1.update(s.getBytes());
        byte[] codedBytes = sha1.digest();
        //将加密后的字节数组转换成字符串
        String codedString = new BigInteger(1, codedBytes).toString(16);

        return codedString;
    }

}
