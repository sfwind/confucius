package com.iquanwai.confucius.biz.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.iquanwai.confucius.biz.exception.ErrorConstants;
import com.iquanwai.confucius.biz.exception.WeixinException;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.wltea.analyzer.IKSegmentation;
import org.wltea.analyzer.Lexeme;
import sun.net.util.IPAddressUtil;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by justin on 16/8/7.
 */
public class CommonUtils {
    public static DecimalFormat priceFormat;
    static {
        priceFormat = new DecimalFormat("0.00");
        priceFormat.setRoundingMode(RoundingMode.HALF_UP);
    }

    public static String placeholderReplace(String content, Map<String, String> replacer) {
        if (StringUtils.isNotEmpty(content) && replacer != null) {
            for (Map.Entry<String, String> entry : replacer.entrySet()) {
                content = StringUtils.replace(content, "{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return content;
    }

    public static Map<String, Object> jsonToMap(String json) {
        if (StringUtils.isEmpty(json)) {
            return Maps.newHashMap();
        }
        Map<String, Object> gsonMap = new Gson().fromJson(json,
                new TypeToken<Map<String, Object>>() {
                }.getType());
        return gsonMap;
    }

    public static String mapToJson(Map<String, Object> map) {
        if (MapUtils.isEmpty(map)) {
            return "";
        }
        String json = new Gson().toJson(map,
                new TypeToken<Map<String, Object>>() {
                }.getType());
        return json;
    }

    public static boolean isError(String json) throws WeixinException {
        if (StringUtils.isEmpty(json)) {
            return false;
        }
        Map<String, Object> gsonMap = jsonToMap(json);
        if (gsonMap.get("errcode") != null && gsonMap.get("errmsg") != null) {
            Integer errcode;
            try {
                errcode = ((Double) gsonMap.get("errcode")).intValue();
            } catch (Exception e) {
                errcode = Integer.valueOf((String) gsonMap.get("errcode"));
            }
            if (errcode.equals(ErrorConstants.ACCESS_TOKEN_EXPIRED)) {
                throw new WeixinException(ErrorConstants.ACCESS_TOKEN_EXPIRED, "accessToken过期了");
            }
            if (errcode.equals(ErrorConstants.ACCESS_TOKEN_INVALID)) {
                throw new WeixinException(ErrorConstants.ACCESS_TOKEN_INVALID, "accessToken失效了");
            }

            return errcode != 0;
        }
        return false;
    }

    public static String jsSign(final Map<String, String> map) {
        if (map == null) {
            return "";
        }
        List<String> list = new ArrayList(map.keySet());
        Collections.sort(list);

        List<String> kvList = Lists.transform(list, input -> input + "=" + map.get(input));

        String digest = StringUtils.join(kvList.iterator(), "&");
        return MessageDigestHelper.getSHA1String(digest);
    }

    public static String randomString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static String sign(final Map<String, String> params) {
        List<String> list = new ArrayList(params.keySet());
        Collections.sort(list);

        List<String> kvList = Lists.transform(list, input -> input + "=" + params.get(input));

        String digest = StringUtils.join(kvList.iterator(), "&")
                .concat("&key=")
                .concat(ConfigUtils.getAPIKey());

        return MessageDigestHelper.getMD5String(digest);
    }

    public static String signH5Pay(final Map<String, String> params) {
        List<String> list = new ArrayList(params.keySet());
        Collections.sort(list);

        List<String> kvList = Lists.transform(list, input -> input + "=" + params.get(input));

        String digest = StringUtils.join(kvList.iterator(), "&");

        return MessageDigestHelper.getMD5String(digest);

    }

    //保留两位小数
    public static Double substract(Double a, Double b) {
        if (a == null || b == null) {
            return null;
        }

        return new BigDecimal(a).subtract(new BigDecimal(b)).
                setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    }

    public static String removeHTMLTag(String html) {
        if (html == null) {
            return "";
        }
        return StringUtils.removePattern(html, "<[^>]*>");
    }

    public static String replaceHttpsDomainName(String content) {
        String temp = StringUtils.replace(content, "http://www.iqycamp.com", "https://www.iqycamp.com");
        return StringUtils.replace(temp, "http://static.iqycamp.com", "https://static.iqycamp.com");
    }


    public static boolean internalIp(String ip) {
        byte[] addr = IPAddressUtil.textToNumericFormatV4(ip);
        final byte b0 = addr[0];
        final byte b1 = addr[1];
        //10.x.x.x/8
        final byte SECTION_1 = 0x0A;
        //172.16.x.x/12
        final byte SECTION_2 = (byte) 0xAC;
        final byte SECTION_3 = (byte) 0x10;
        final byte SECTION_4 = (byte) 0x1F;
        //192.168.x.x/16
        final byte SECTION_5 = (byte) 0xC0;
        final byte SECTION_6 = (byte) 0xA8;
        switch (b0) {
            case SECTION_1:
                return true;
            case SECTION_2:
                if (b1 >= SECTION_3 && b1 <= SECTION_4) {
                    return true;
                }
            case SECTION_5:
                switch (b1) {
                    case SECTION_6:
                        return true;
                    default:
                        break;
                }
            default:
                return false;
        }
    }

    public static List<String> separateWords(String words) {
        List<String> usefulWords = Lists.newArrayList();

        IKSegmentation ikSeg = new IKSegmentation(new StringReader(words), false);
        try {
            Lexeme l;
            while ((l = ikSeg.next()) != null) {
                //找出词语,数字,英文单词
                if (l.getLexemeType() == Lexeme.TYPE_CJK_NORMAL || l.getLexemeType() == Lexeme.TYPE_NUM ||
                        l.getLexemeType() == Lexeme.TYPE_LETTER) {
                    usefulWords.add(l.getLexemeText().toUpperCase());
                }
            }
        } catch (IOException e) {
            // ignore
        }

        return usefulWords;
    }

    /**
     * 过滤emoji 或者 其他非文字类型的字符
     */
    public static String filterEmoji(String source) {
        //到这里铁定包含
        StringBuilder buf = null;
        int len = source.length();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            if (isEmojiCharacter(codePoint)) {
                if (buf == null) {
                    buf = new StringBuilder(source.length());
                }
                buf.append(codePoint);
            }
        }
        if (buf == null) {
            return source;//如果没有找到 emoji表情，则返回源字符串
        } else {
            if (buf.length() == len) {//这里的意义在于尽可能少的toString，因为会重新生成字符串
                return source;
            } else {
                return buf.toString();
            }
        }

    }

    private static boolean isEmojiCharacter(char codePoint) {
        return (codePoint == 0x0) ||
                (codePoint == 0x9) ||
                (codePoint == 0xA) ||
                (codePoint == 0xD) ||
                ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
                ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));
    }

    public static String formatePrice(Double fee) {
        return priceFormat.format(fee);
    }

}
