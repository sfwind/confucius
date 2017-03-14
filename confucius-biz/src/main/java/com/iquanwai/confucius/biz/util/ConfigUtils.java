package com.iquanwai.confucius.biz.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class ConfigUtils {
	private static Config config;
	private static Config localconfig;
	private static Config fileconfig;

	private static Timer timer;
	static{
		loadConfig();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				loadConfig();
			}
		}, 0, 1000*60);
	}

	private static void loadConfig() {
		localconfig = ConfigFactory.load("localconfig");
		config = ConfigFactory.load("confucius");
		fileconfig = ConfigFactory.parseFile(new File("/data/config/localconfig"));
		config = localconfig.withFallback(config);
		config = fileconfig.withFallback(config);
	}

	public static String getAppid() {
		return config.getString("appid");
	}

	public static boolean logSwitch() {
		return config.getBoolean("open.log");
	}

	public static boolean pressTestSwitch(){
		return config.getBoolean("press.test");
	}

	public static String getAPIKey() {
		return config.getString("api.key");
	}

	public static String getSecret() {
		return config.getString("secret");
	}

	public static int getJsSignatureInterval() {
		return config.getInt("js.internal");
	}

	public static boolean isDebug(){
		return config.getBoolean("debug")||config.getBoolean("press.test");
	}

	public static boolean isFrontDebug(){
		return config.getBoolean("front.debug");
	}

	public static boolean logDetail(){
		return config.getBoolean("log.debug");
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

	public static String signupSuccessMsgKey(){
		return config.getString("signup.success.msg");
	}

	public static String angelMsgKey(){
		return config.getString("angel.msg");
	}

	public static String incompleteTaskMsgKey(){
		return config.getString("incomplete.task.msg");
	}

	public static boolean messageSwitch(){
		return config.getBoolean("message.switch");
	}

	public static String coursePassMsgKey(){
		return config.getString("course.pass.msg");
	}

	public static String domainName(){
		return config.getString("app.domain");
	}

	public static String adapterDomainName(){
		return config.getString("adapter.domain");
	}

	public static String resourceDomainName(){
		return config.getString("resource.domain");
	}

	public static String streamResourceDomainName(){
		return config.getString("stream.resource.domain");
	}

	public static String realDomainName(){
		return config.getString("app.domainname");
	}

	public static String staticResourceUrl(){
		String url = config.getString("static.resource.url");
		//测试环境防浏览器缓存，添加随机参数
		if(url.endsWith("?")){
			url = url.concat("_t=").concat(new Random().nextInt()+"");
		}

		return url;
	}

	public static String staticPcResourceUrl(){
		String url = config.getString("static.pc.resource.url");
		//测试环境防浏览器缓存，添加随机参数
		if(url.endsWith("?")){
			url = url.concat("_t=").concat(new Random().nextInt()+"");
		}
		return url;
	}

	public static String gaId(){
		return config.getString("ga.id");
	}

	public static String getDefaultOpenid(){
		return config.getString("default.openid");
	}

	public static String getValue(String key){
		return config.getString(key);
	}

	public static List<Integer> getNeedAngelClasses(){
		return config.getIntList("class.need.angel");
	}

	public static String getLoginSalt(){
		return config.getString("login.salt");
	}

	public static String getLoginSocketUrl(){
		return config.getString("static.pc.socket.url");
	}

	public static List<Integer> getWorkScoreList(){
		return config.getIntList("work.difficulty.score");
	}

	public static Integer getChallengeScore(){
		return config.getInt("challenge.score");
	}

	public static Boolean isDevelopment(){
		return config.getBoolean("development");
	}

	public static Boolean isPcMaintenance(){
		return config.getBoolean("pc.server.maintenance");
	}

	public static String getSurveyUrl(Integer id){
		try {
			return config.getString("wjx.survey." + id);
		} catch (Exception e){
			return null;
		}
	}

	public static Integer getFeedBackId(){
		return config.getInt("wjx.feedback");
	}

	public static String willCloseMsgKey(){
		return config.getString("will.close.task.msg");
	}

	public static String accountChangeMsgKey(){
		return config.getString("account.change.msg");
	}

//	public static Integer getFormalCourseId(Integer auditionId){
//		try{
//			return config.getInt("audition.formal.course.mapping."+auditionId);
//		} catch (Exception e){
//			return null;
//		}
//	}

	public static Integer getProfileFullScore(){
		return config.getInt("profile.full.score");
	}

	public static String activityStartMsgKey(){
		return config.getString("activity.start.msg");
	}

	public static String getQRCodeImgDomain(){
		return config.getString("qr.code.image.domain");
	}
}
