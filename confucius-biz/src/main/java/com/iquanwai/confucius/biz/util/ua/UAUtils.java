package com.iquanwai.confucius.biz.util.ua;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by nethunder on 2017/5/23.
 */
public class UAUtils {
    private static Parser parser = null;
    private static Logger logger = LoggerFactory.getLogger(UAUtils.class);
    static {
        try {
            parser = new Parser();
        } catch (IOException e) {
            logger.error("读取ua配置文件失败!");
        }
    }

    public static OS parseOs(String ua){
        if (parser == null) {
            logger.error("读取ua配置文件失败!无法获得os!");
            return null;
        } else {
            return parser.parseOS(ua);
        }
    }

    public static boolean isLowerAndroid(String ua,Integer major,Integer minor){
        try {
            OS os = UAUtils.parseOs(ua);
            if (os == null) {
                logger.error("获取os失败,{}", ua);
                return false;
            }
            logger.info("os:{}", os);
            if (os.family != null && os.major != null && os.minor != null) {
                String family = os.family.toLowerCase();
                Integer tempMajor = Double.valueOf(os.major).intValue();
                Integer tempMinor = Double.valueOf(os.minor).intValue();
                return "android".equals(family) && (tempMajor < major || (tempMajor.equals(major) && tempMinor < minor));
            } else {
                return false;
            }
        } catch(Exception e){
            logger.error("error for parse os", e);
            return false;
        }
    }

}
