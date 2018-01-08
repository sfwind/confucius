package com.iquanwai.confucius.biz.util;

import com.iquanwai.confucius.biz.domain.weixin.accesstoken.AccessTokenService;
import com.iquanwai.confucius.biz.exception.WeixinException;
import com.rabbitmq.client.TrustEverythingTrustManager;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;

/**
 * Created by justin on 8/3/16.<br/>
 * Description: HTTP请求封装
 */
@Service
public class RestfulHelper {
    @Autowired
    private AccessTokenService accessTokenService;

    private static OkHttpClient client = new OkHttpClient();

    private static MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static MediaType XML = MediaType.parse("text/xml; charset=utf-8");

    private Logger logger = LoggerFactory.getLogger(RestfulHelper.class);

    private OkHttpClient sslClient;

    @PostConstruct
    public void init() {
        try {
            if (!ConfigUtils.isDebug()) {
                initCert();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 发起POST请求,requestUrl中的{access_token}字段会被替换成缓存的accessToken<br/>
     * 触发WeixinException时会刷新AccessToken并重新调用
     * @param requestUrl 请求链接
     * @param json 请求参数
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
     * @param requestUrl 请求的url
     * @param xml 参数
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
     * @param requestUrl 请求url，参数需要手动拼接到url中
     * @return 响应体
     */
    public String get(String requestUrl) {
        if (StringUtils.isNotEmpty(requestUrl)) {
            String accessToken = accessTokenService.getAccessToken();
            logger.info("accesstoken is :{}", accessToken);
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

    public ResponseBody getPlain(String requestUrl) {
        if (StringUtils.isNotEmpty(requestUrl)) {
            Request request = new Request.Builder()
                    .url(requestUrl)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return response.body();
            } catch (Exception e) {
                logger.error("execute " + requestUrl + " error", e);
            }
        }
        return null;
    }


    public String sslPostXml(String requestUrl, String xml) {
        logger.info("requestUrl: {}\nxml: {}", requestUrl, xml);
        if (StringUtils.isNotEmpty(requestUrl) && StringUtils.isNotEmpty(xml)) {
            Request request = new Request.Builder()
                    .url(requestUrl)
                    .post(RequestBody.create(XML, xml))
                    .build();

            try {
                Response response = sslClient.newCall(request).execute();
                String body = response.body().string();
                logger.info("body:{}", body);
                return body;
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        return "";
    }

    /**
     * 上传微信素材
     * @param multipartFile
     * @param url
     * @return
     */
    public String uploadWXFile(MultipartFile multipartFile,String url) {
        String result = null;
        String accessToken = accessTokenService.getAccessToken();
        logger.info("accesstoken is :{}", accessToken);
        url = url.replace("{access_token}",accessToken);
        try {
            URL url1 = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Cache-Control", "no-cache");
            String boundary = "-----------------------------" + System.currentTimeMillis();
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            OutputStream output = conn.getOutputStream();
            output.write(("--" + boundary + "\r\n").getBytes());
            output.write(
                    String.format("Content-Disposition: form-data; name=\"media\"; filename=\"%s\"\r\n", multipartFile.getOriginalFilename())
                            .getBytes());
            output.write("Content-Type: image/jpeg \r\n\r\n".getBytes());
            byte[] data = new byte[1024];
            int len = 0;
            DataInputStream input = new DataInputStream(multipartFile.getInputStream());
            while ((len = input.read(data)) > -1) {
                output.write(data, 0, len);
            }
            output.write(("\r\n--" + boundary + "\r\n\r\n").getBytes());
            output.flush();
            output.close();
            input.close();
            InputStream resp = conn.getInputStream();
            StringBuffer sb = new StringBuffer();
            while ((len = resp.read(data)) > -1) {
                sb.append(new String(data, 0, len, "utf-8"));
            }
            resp.close();
            result = sb.toString();
        } catch (ClientProtocolException e) {
            logger.error(e.getLocalizedMessage(),e);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return result;
    }


    private void initCert() throws Exception {
        // 证书密码，默认为商户ID
        String key = ConfigUtils.getMch_id();
        // 证书的路径
        String path = "/data/security/apiclient_cert.p12";
        // 指定读取证书格式为PKCS12
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        // 读取本机存放的PKCS12证书文件
        try (FileInputStream instream = new FileInputStream(new File(path))) {
            // 指定PKCS12的密码(商户ID)
            keyStore.load(instream, key.toCharArray());
        }
        SSLContext sslcontext = SSLContexts
                .custom()
                .loadKeyMaterial(keyStore, key.toCharArray())
                .useTLS()
                .build();
        // 指定TLS版本
        SSLSocketFactory sslSocketFactory = sslcontext.getSocketFactory();
        // 设置httpclient的SSLSocketFactory
        sslClient = new OkHttpClient.Builder().sslSocketFactory(sslSocketFactory,
                new TrustEverythingTrustManager()).build();

    }
}
