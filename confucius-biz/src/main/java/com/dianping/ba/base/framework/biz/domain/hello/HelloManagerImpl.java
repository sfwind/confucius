package com.dianping.ba.base.framework.biz.domain.hello;

import com.dianping.ba.base.framework.biz.dao.AccountDao;
import com.dianping.ba.base.framework.biz.dao.po.AccountPO;
import com.dianping.ba.base.framework.biz.mapper.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by justin on 7/23/15.
 */
@Component("hello" )
public class HelloManagerImpl implements HelloManager {

    private Logger logger = LoggerFactory.getLogger(getClass());

//    @Autowired
//    private AccountDao accountDao;

    public String hello(HelloDto helloDto) {
//        AccountPO po = accountDao.query(Mapper.mapDtoToPO(helloDto));
        logger.info("method invoke complete");
        return "hello "+helloDto.getName()+"!";
    }
}
