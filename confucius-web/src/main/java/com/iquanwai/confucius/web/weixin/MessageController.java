package com.iquanwai.confucius.web.weixin;

import com.iquanwai.confucius.biz.domain.weixin.aes.Prpcrypt;
import com.iquanwai.confucius.biz.domain.weixin.aes.Result;
import com.iquanwai.confucius.biz.domain.weixin.message.CallbackMessageService;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by justin on 17/7/6.
 */
@RequestMapping("/wx/callback")
@Controller
public class MessageController {

    private static String INVALID_REQUEST = "invalid_request";
    private static String INVALID_DECRYPT = "invalid_decrypt";
    private static String INVALID_SIGNATURE = "invalid_signature";
    private static String SUCCESS = "success";

    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private CallbackMessageService callbackMessageService;

    @RequestMapping(value = "/message", method = RequestMethod.GET)
    @ResponseBody
    public String openCallbackMode(@PathVariable String app, HttpServletRequest request, HttpServletResponse response) {

        //获取请求参数
        String signature = request.getParameter("msg_signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String echostring = request.getParameter("echostr");
        if (signature == null || timestamp == null ||
                nonce == null || echostring == null) {
            return INVALID_REQUEST;
        }

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
            sha1.update(total.getBytes());
            byte[] codedBytes = sha1.digest();
            //将加密后的字节数组转换成字符串
            String codedString = new BigInteger(1, codedBytes).toString(16);
            //将加密的结果与请求参数中的signature比对，如果相同，原样返回echostr参数内容
            if (codedString.equals(signature)) {
                Result result;
                try {
//                echostring = aesManager.decrypt(echostring);
                    LOGGER.info("aeskey is "+ ConfigUtils.getEncodingAESKey());
                    Prpcrypt crypt = new Prpcrypt(ConfigUtils.getEncodingAESKey());
                    result = crypt.decrypt(echostring, "");
                } catch (Exception e) {
                    LOGGER.error("decrypt error", e);
                    return INVALID_DECRYPT;
                }
                if (result != null && !result.getResult().isEmpty()) {
                    LOGGER.info("result is "+result.getResult());
                    return result.getResult();
                } else if(result != null){
                    LOGGER.info("error code is "+result.getCode());
                }
            }
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("NoSuchAlgorithmException", e);
        }


        return INVALID_SIGNATURE;
    }


    @RequestMapping(value = "/message", method = RequestMethod.POST)
    @ResponseBody
    public String receiveCallback(@PathVariable String app, HttpServletRequest request, HttpServletResponse response) {

        try {
            // 获取请求和响应
            InputStream is = request.getInputStream();

            // 从输入流解析document
            Document document = XMLHelper.parseDocument(is);
            String xml = XMLHelper.convertDocumentToString(document);
            // 转换成string后关闭
            is.close();

            //获取请求参数
            String signature = request.getParameter("msg_signature");
            String timestamp = request.getParameter("timestamp");
            String nonce = request.getParameter("nonce");
            if (signature == null || timestamp == null ||
                    nonce == null) {
                return INVALID_REQUEST;
            }

            // 加密xml
            String encryptedXml = xml;
            // weixin token
            String token = ConfigUtils.getToken();

            Result result = Prpcrypt.fromTencent(encryptedXml, ConfigUtils.getEncodingAESKey(), signature, token, timestamp, nonce, "");
            if (result.getCode() != 0) {
                return INVALID_REQUEST;
            }

            String decryptedXml = result.getResult();
            LOGGER.info("decrypted xml is \n" + decryptedXml);
            Document decryptedDocument = XMLHelper.convertStringToDocument(decryptedXml);
            callbackMessageService.handleCallback(decryptedDocument);
        } catch (Exception e) {
            LOGGER.error("received user message failed", e);
        }

        return SUCCESS;
    }
}
