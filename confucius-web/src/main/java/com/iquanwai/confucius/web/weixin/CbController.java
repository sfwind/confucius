package com.iquanwai.confucius.web.weixin;


import com.iquanwai.confucius.biz.aes.Prpcrypt;
import com.iquanwai.confucius.biz.aes.Result;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Controller
@RequestMapping("/wx")
public class CbController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CbController.class);
    private static String INVALID_REQUEST = "invalid parameter!";
    private static String SUCCESS = "success";

    @RequestMapping("/cb")
    public void doGet(HttpServletRequest request, HttpServletResponse response) {

        //获取请求参数
        String signature = request.getParameter("msg_signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String echostring = request.getParameter("echostr");
        if (signature == null || timestamp == null ||
                nonce == null || echostring == null) {
            sendResponse(response, INVALID_REQUEST);
            return;
        }

        //你自己填写的token
        String token = ConfigUtils.getToken();


        //对请求参数和自己的token进行排序，并连接排序后的结果为一个字符串
        String[] strSet = new String[]{token, timestamp, nonce, echostring};
        Arrays.sort(strSet);
        String total = "";
        for (String string : strSet) {
            total = total + string;
        }
        //SHA-1加密实例
        MessageDigest sha1 = null;
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("NoSuchAlgorithmException", e);
        }
        sha1.update(total.getBytes());
        byte[] codedBytes = sha1.digest();
        //将加密后的字节数组转换成字符串
        String codedString = new BigInteger(1, codedBytes).toString(16);
        //将加密的结果与请求参数中的signature比对，如果相同，原样返回echostr参数内容
        if (codedString.equals(signature)) {
            Result result;
            try {
//                echostring = aesManager.decrypt(echostring);
                Prpcrypt pc = new Prpcrypt(ConfigUtils.getEncodingAesKey());
                result = pc.decrypt(echostring, "");

            } catch (Exception e) {
                LOGGER.error("decrypt error", e);
                return;
            }
            if (result != null && !result.getResult().isEmpty()) {
                sendResponse(response, result.getResult());
            }
        }
    }

    private void sendResponse(HttpServletResponse response, String responseContents) {
        BufferedWriter resBr = null;
        try {
            OutputStream os = response.getOutputStream();
            resBr = new BufferedWriter(new OutputStreamWriter(os));
            resBr.write(responseContents);
            resBr.flush();
        } catch (IOException e) {
            LOGGER.error("IOException", e);
        } finally {
            if (resBr != null) {
                try {
                    resBr.close();
                } catch (IOException e) {
                    LOGGER.error("close bufferWriter error", e);
                }
            }
        }
    }


}

