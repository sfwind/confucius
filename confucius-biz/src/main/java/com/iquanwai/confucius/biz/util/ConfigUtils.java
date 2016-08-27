package com.iquanwai.confucius.biz.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ConfigUtils {
	private static Config config;
	private static Config localconfig;
	static{
		localconfig = ConfigFactory.load("localconfig");
		config = ConfigFactory.load("confucius");
		config = localconfig.withFallback(config);
	}

	public static String getToken() {
		return config.getString("token");
	}

	public static String getEncodingAesKey() {
		return "NdljBZaXGGkx8c9R70fpZ54M6s1OHlxxpKMG7bIoadd";
	}

	public static String getAppid() {
		return config.getString("appid");
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
}
