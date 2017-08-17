package com.iquanwai.confucius.biz.util;

import com.iquanwai.confucius.biz.domain.weixin.accessToken.AccessTokenService;
import com.iquanwai.confucius.biz.exception.WeixinException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Created by justin on 8/3/16.<br/>
 * Description: HTTP请求封装
 */
@Service
public class RestfulHelper {
    @Autowired
    private AccessTokenService accessTokenService;

    private static OkHttpClient client = new OkHttpClient();

    private MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private MediaType XML = MediaType.parse("text/xml; charset=utf-8");

    private Logger logger = LoggerFactory.getLogger(RestfulHelper.class);

    /**
     * 发起POST请求,requestUrl中的{access_token}字段会被替换成缓存的accessToken<br/>
     * 触发WeixinException时会刷新AccessToken并重新调用
     *
     * @param requestUrl 请求链接
     * @param json       请求参数
     * @return 响应体
     */
    public String post(String requestUrl, String json) {
        if (StringUtils.isNotEmpty(requestUrl) && StringUtils.isNotEmpty(json)) {
            String accessToken = accessTokenService.getAccessToken();
            String url = requestUrl.replace("{access_token}", accessToken);
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(JSON, json))
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                try {
                    if (CommonUtils.isError(body)) {
                        logger.error("execute {} return error, error message is {}", url, body);
                    }
                } catch (WeixinException e) {
                    //refresh token and try again
                    accessToken = accessTokenService.refreshAccessToken(false);
                    url = requestUrl.replace("{access_token}", accessToken);
                    request = new Request.Builder()
                            .url(url)
                            .post(RequestBody.create(JSON, json))
                            .build();
                    response = client.newCall(request).execute();
                    body = response.body().string();
                    if (CommonUtils.isError(body)) {
                        logger.error("execute {} return error, error message is {}", url, body);
                    }
                }
                return body;
            } catch (Exception e) {
                logger.error("execute " + requestUrl + " error", e);
            }
        }
        return "";
    }

    /**
     * 发起POST请求
     *
     * @param requestUrl 请求的url
     * @param xml        参数
     * @return 响应体
     */
    public String postXML(String requestUrl, String xml) {
        logger.info("requestUrl: {}\nxml: {}", requestUrl, xml);
        if (StringUtils.isNotEmpty(requestUrl) && StringUtils.isNotEmpty(xml)) {
            Request request = new Request.Builder()
                    .url(requestUrl)
                    .post(RequestBody.create(XML, xml))
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String body = response.body().string();
//                if(CommonUtils.isError(body)){
//                    logger.error("execute {} return error, error message is {}", requestUrl, body);
//                }
                logger.info("body:{}", body);
                return body;
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        return "";
    }


    /**
     * 发起GET请求,requestUrl中的{access_token}字段会被替换成缓存的accessToken<br/>
     * 触发WeixinException时会刷新AccessToken并重新调用
     *
     * @param requestUrl 请求url，参数需要手动拼接到url中
     * @return 响应体
     */
    public String get(String requestUrl) {
        if (StringUtils.isNotEmpty(requestUrl)) {
            String accessToken = accessTokenService.getAccessToken();
            logger.info("accessToken is :{}", accessToken);
            String url = requestUrl.replace("{access_token}", accessToken);
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                try {
                    if (CommonUtils.isError(body)) {
                        logger.error("execute {} return error, error message is {}", url, body);
                    }
                } catch (WeixinException e) {
                    //refresh token and try again
                    accessToken = accessTokenService.refreshAccessToken(false);
                    url = requestUrl.replace("{access_token}", accessToken);
                    request = new Request.Builder()
                            .url(url)
                            .build();
                    response = client.newCall(request).execute();
                    body = response.body().string();
                    if (CommonUtils.isError(body)) {
                        logger.error("execute {} return error, error message is {}", url, body);
                    }
                }
                return body;
            } catch (Exception e) {
                logger.error("execute " + requestUrl + " error", e);
            }
        }
        return "";
    }

    public String getPlain(String requestUrl) {
        if (StringUtils.isNotEmpty(requestUrl)) {
            Request request = new Request.Builder()
                    .url(requestUrl)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String body = response.body().string();

                return body;
            } catch (Exception e) {
                logger.error("execute " + requestUrl + " error", e);
            }
        }
        return "";
    }

    public String risePlanChoose(String cookieName, String cookieValue, Integer problemId) {
        Assert.notNull(problemId);
        String url = ConfigUtils.domainName() + "/rise/plan/choose/problem/" + problemId;
        return postRise(url, "{\"default\":\"null\"}", cookieName, cookieValue);
    }


    public String postRise(String url, String json, String cookieName, String cookieValue) {
//        if (callback == null || callback.getOpenid() == null) {
//            logger.error("调用rise接口异常，没有身份信息,callbackId:{}", callback != null ? callback.getState() : null);
//            return "";
//        }
//        String cookieName = "";
//        String cookieValue = "";
//        if (callback.getAccessToken() != null) {
//            cookieName = OAuthService.ACCESS_TOKEN_COOKIE_NAME;
//            cookieValue = callback.getAccessToken();
//        } else {
//            cookieName = OAuthService.QUANWAI_TOKEN_COOKIE_NAME;
//            cookieValue = callback.getPcAccessToken();
//        }
        String cookie = cookieName + "=" + cookieValue;

        if (StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(json)) {
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(JSON, json))
                    .addHeader("Cookie", cookie)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                logger.error("execute " + url + " error", e);
            }
        }
        return "";
    }

}
