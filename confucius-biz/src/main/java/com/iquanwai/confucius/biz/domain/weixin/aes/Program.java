package com.iquanwai.confucius.biz.domain.weixin.aes;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.UUID;

public class Program {

    /**
     * 1. 第三方回复加密消息给公众平台
     * 2. 第三方收到公众平台发送的消息，验证消息的安全性，并对消息进行解密。
     * 说明：异常java.security.InvalidKeyException:illegal Key Size的解决方案： 1.
     * 在官方网站下载JCE无限制权限策略文件（JDK7的下载地址：
     * http://www.oracle.com/technetwork/java/javase
     * /downloads/jce-7-download-432124.html 2.
     * 下载后解压，可以看到local_policy.jar和US_export_policy.jar以及readme.txt 3.
     * 如果安装了JRE，将两个jar文件放到%JRE_HOME%\lib\security目录下覆盖原来的文件 4.
     * 如果安装了JDK，将两个jar文件放到%JDK_HOME%\jre\lib\security目录下覆盖原来文件
     */
    public static void main(String[] args) throws Exception {

        //
        // 第三方回复公众平台
        //

        // 需要加密的明文
        String encodingAesKey = "NdljBZaXGGkx8c9R70fpZ54M6s1OHlxxpKMG7bIoadd";
        String token = "dianpingqywx";
        String timestamp = "1409304348";
//        System.out.print(System.currentTimeMillis() / 1000);
        String nonce = UUID.randomUUID().toString().substring(0,8);
        String appid = "15";
        String to_xml = " 中文<xml><ToUserName><![CDATA[oia2TjjewbmiOUlr6X-1crbLOvLw]]></ToUserName><FromUserName><![CDATA[gh_7f083739789a]]></FromUserName><CreateTime>1407743423</CreateTime><MsgType><![CDATA[video]]></MsgType><Video><MediaId><![CDATA[eYJ1MbwPRJtOvIEabaxHs7TX2D-HV71s79GUxqdUkjm6Gs2Ed1KF3ulAOA9H1xG0]]></MediaId><Title><![CDATA[testCallBackReplyVideo]]></Title><Description><![CDATA[testCallBackReplyVideo]]></Description></Video></xml>";

        Result result = Prpcrypt.toTencent(to_xml, encodingAesKey, nonce, token, appid, timestamp);
        String miwen = result.getResult();
        System.out.println("加密后: " + miwen);


        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        StringReader sr = new StringReader(miwen);
        InputSource is = new InputSource(sr);
        Document document = db.parse(is);

        Element root = document.getDocumentElement();
        NodeList nodelist1 = root.getElementsByTagName("Encrypt");
        NodeList nodelist2 = root.getElementsByTagName("MsgSignature");

        String encrypt = nodelist1.item(0).getTextContent();
        String msgSignature = nodelist2.item(0).getTextContent();

        String format = "<xml><ToUserName><![CDATA[toUser]]></ToUserName><Encrypt><![CDATA[%1$s]]></Encrypt></xml>";
        String fromXML = String.format(format, encrypt);

        //
        // 公众平台发送消息给第三方，第三方处理
        //

        // 第三方收到公众号平台发送的消息
        Result result2 = Prpcrypt.fromTencent(fromXML, encodingAesKey, msgSignature, token,
                timestamp, nonce, appid);
        System.out.println("错误码: " + result2.getCode());
        System.out.println("解密后明文: " + result2.getResult());
    }
}
