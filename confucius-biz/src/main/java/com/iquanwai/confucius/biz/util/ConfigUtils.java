package com.iquanwai.confucius.biz.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.util.Random;

public class ConfigUtils {
	private static Config config;
	private static Config localconfig;
	private static Config fileconfig;
	static{
		localconfig = ConfigFactory.load("localconfig");
		config = ConfigFactory.load("confucius");
		fileconfig = ConfigFactory.parseFile(new File("/data/config/localconfig"));
		config = localconfig.withFallback(config);
		config = fileconfig.withFallback(config);
	}

	public static String getAppid() {
		return config.getString("appid");
	}

	public static String getAPIKey() {
		return config.getString("api.key");
	}

	public static String getSecret() {
		return config.getString("secret");
	}

	public static String getRedirectUrl(){
		return config.getString("redirectUrl");
	}

	public static int getJsSignatureInterval() {
		return config.getInt("js.internal");
	}

	public static boolean isDebug(){
		return config.getBoolean("debug");
	}

	public static String getMch_id(){
		return config.getString("mch_id");
	}

	public static String getExternalIP(){
		return config.getString("external.ip");
	}

	public static Integer getBillOpenMinute(){
		return config.getInt("bill.open.minute");
	}

	public static String getPayResultCallbackUrl(){
		return config.getString("pay.result.callback.url");
	}

	public static String staticResourceUrl(){
		String url = config.getString("static.resource.url");
		//测试环境防浏览器缓存，添加随机参数
		if(url.endsWith("?")){
			url = url.concat("_t=").concat(new Random().nextInt()+"");
		}

		return url;
	}

	public static String getDefaultOpenid(){
		return config.getString("default.openid");
	}

	public static String getValue(String key){
		return config.getString(key);
	}
}
