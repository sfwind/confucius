package com.iquanwai.confucius.biz.util;

import com.iquanwai.confucius.biz.domain.weixin.accessToken.AccessTokenService;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by justin on 8/3/16.
 */
@Service
public class RestfulHelper {
    @Autowired
    private AccessTokenService accessTokenService;

    private static OkHttpClient client = new OkHttpClient();

    private MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private MediaType XML = MediaType.parse("text/xml; charset=utf-8");

    private Logger logger = LoggerFactory.getLogger(RestfulHelper.class);

    public String post(String requestUrl, String json) {
        if(StringUtils.isNotEmpty(requestUrl) && StringUtils.isNotEmpty(json)) {
            String accessToken = accessTokenService.getAccessToken();
            String url = requestUrl.replace("{access_token}", accessToken);
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(JSON, json))
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                if(CommonUtils.isError(body)){
                    logger.error("execute {} return error, error message is {}", url, body);
                }
                return body;
            } catch (Exception e) {
                logger.error("execute " + requestUrl + " error", e.getCause().getMessage());
            }
        }
        return "";
    }

    public String postXML(String requestUrl, String xml) {
        logger.info("requestUrl: {}\nxml: {}",requestUrl, xml);
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(requestUrl);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("Content-Type","text/xml; charset=utf-8");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(xml);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            logger.error("execute " + requestUrl + " error", e);
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                // ignore
            }
        }
        return result;
    }


    public String get(String requestUrl) {
        if(StringUtils.isNotEmpty(requestUrl)) {
            String accessToken = accessTokenService.getAccessToken();
            String url = requestUrl.replace("{access_token}", accessToken);
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                if(CommonUtils.isError(body)){
                    logger.error("execute {} return error, error message is {}", url, body);
                }
                return body;
            } catch (Exception e) {
                logger.error("execute " + requestUrl + " error", e);
            }
        }
        return "";
    }

}
