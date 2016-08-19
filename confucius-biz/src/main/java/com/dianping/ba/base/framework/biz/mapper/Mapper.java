package com.dianping.ba.base.framework.biz.mapper;

import com.dianping.ba.base.framework.biz.dao.po.AccountPO;
import com.dianping.ba.base.framework.biz.domain.hello.HelloDto;
import org.modelmapper.ModelMapper;

/**
 * Created by justin on 7/23/15.
 */
public class Mapper {
    public static AccountPO mapDtoToPO(HelloDto helloDto){
        if(helloDto == null) return null;
        ModelMapper modelMapper  =  new ModelMapper();
        AccountPO accountPO = modelMapper.map(helloDto, AccountPO.class);
        return accountPO;
    }
}
