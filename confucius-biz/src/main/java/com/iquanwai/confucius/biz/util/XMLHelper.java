package com.iquanwai.confucius.biz.util;


import com.iquanwai.confucius.biz.domain.weixin.pay.UnifiedOrderReply;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by justin on 14-7-24.
 */
public class XMLHelper {
    public static <T> String createXML(T t){
        StringWriter sw = new StringWriter();
        try {
            JAXBContext jbc = JAXBContext.newInstance(t.getClass());   //传入要转换成xml的对象类型
            Marshaller mar = jbc.createMarshaller();
            mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);//是否格式化生成的xml串
            mar.setProperty(Marshaller.JAXB_FRAGMENT, true);
            mar.marshal(t, sw);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return sw.toString();
    }

    public static <T> T parseXml(Class<T> clazz, String xml) {
        try {
            JAXBContext jc = JAXBContext.newInstance(clazz);
            Unmarshaller u = jc.createUnmarshaller();
            return (T) u.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String appendCDATA(String value){
        return "<![CDATA["+value+"]]>";
    }

    public static void main(String[] args) {
        String reply = "<xml>\n" +
                "<return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "<return_msg><![CDATA[OK]]></return_msg>\n" +
                "<appid><![CDATA[wx2421b1c4370ec43b]]></appid>\n" +
                "<mch_id><![CDATA[10000100]]></mch_id>\n" +
                "<nonce_str><![CDATA[IITRi8Iabbblz1Jc]]></nonce_str>\n" +
                "<sign><![CDATA[7921E432F65EB8ED0CE9755F0E86D72F]]></sign>\n" +
                "<result_code><![CDATA[SUCCESS]]></result_code>\n" +
                "<prepay_id><![CDATA[wx201411101639507cbf6ffd8b0779950874]]></prepay_id>\n" +
                "<trade_type><![CDATA[JSAPI]]></trade_type>\n" +
                "</xml>";
        UnifiedOrderReply reply1= parseXml(UnifiedOrderReply.class, reply);
    }
}
