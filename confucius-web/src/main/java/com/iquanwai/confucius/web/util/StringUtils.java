package com.iquanwai.confucius.web.util;

public class StringUtils {

    public static boolean hasChinese(String str){
       if(str==null){
           return false;
       }
       for(char c : str.toCharArray()){
           if(c >= 0x4E00 &&  c <= 0x9FA5){
               return true;
           }
       }
       return false;
    }
}
