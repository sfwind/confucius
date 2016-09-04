package com.iquanwai.confucius.biz.domain.weixin.account;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * Created by justin on 16/8/10.
 */
@Service
public class AccountServiceImpl implements AccountService {
    @Autowired
    public RestfulHelper restfulHelper;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public Account getAccount(String openId) {
        String url = USER_INFO_URL;
        Map<String, String> map = Maps.newHashMap();
        map.put("openid", openId);
        url = CommonUtils.urlReplace(url, map);
        Account account = new Account();

        String body = restfulHelper.get(url);
        Map<String, Object> result = CommonUtils.jsonToMap(body);

        try {
            ConvertUtils.register(new Converter() {
                public Object convert(Class aClass, Object value) {
                    if (value == null)
                        return null;

                    if (!(value instanceof Double)) {
                        logger.error("不是日期类型");
                        throw new ConversionException("不是日期类型");
                    }
                    Double time = (Double)value;

                    return new DateTime(time.longValue()).toDate();
                }
            }, Date.class);

            BeanUtils.populate(account, result);
            return account;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

}
