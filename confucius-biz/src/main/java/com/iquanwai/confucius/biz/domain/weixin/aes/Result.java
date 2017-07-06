package com.iquanwai.confucius.biz.domain.weixin.aes;

public class Result {
	int code;
	String result;

	public Result(int code, String result) {
		this.code = code;
		this.result = result;
	}

	public int getCode() {
		return code;
	}
	

	public String getResult() {
		return result;
	}
	
	
}
