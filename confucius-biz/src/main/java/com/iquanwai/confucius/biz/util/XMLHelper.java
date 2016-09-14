package com.iquanwai.confucius.biz.util;


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
        String escaped_xml = sw.toString();

        return escaped_xml.replace("&lt;![CDATA", "<![CDATA").replace("]]&gt;", "]]>");
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
                "    <appid>wx6d7641af1b854a21</appid>\n" +
                "    <body>圈外-线上课程</body>\n" +
                "    <detail>&lt;![CDATA[{\"goodsDetail\":[{\"goods_id\":\"1\",\"goods_name\":\"结构化思维\n" +
                "\",\"goods_num\":1,\"price\":1000}]}]]&gt;</detail>\n" +
                "    <mch_id>1388290502</mch_id>\n" +
                "    <nonce_str>okn4i6h57vxux6hz</nonce_str>\n" +
                "    <notify_url>http://www.confucius.mobi/wx/pay/result/callback</notify_url>\n" +
                "    <openid>oK881wQekezGpw6rq790y_vAY_YY</openid>\n" +
                "    <out_trade_no>1cp11l48476ymz83</out_trade_no>\n" +
                "    <sign>2623685A16ADB47279096BC72F220CCA</sign>\n" +
                "    <spbill_create_ip>121.43.177.170</spbill_create_ip>\n" +
                "    <time_expire>20160914235903</time_expire>\n" +
                "    <time_start>20160914232903</time_start>\n" +
                "    <total_fee>1000</total_fee>\n" +
                "    <trade_type>JSAPI</trade_type>\n" +
                "</xml>";
        System.out.println(reply.replace("&lt;![CDATA", "<![CDATA").replace("]]&gt;", "]]>"));
    }
}
