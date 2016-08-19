package com.dianping.ba.base.framework.biz.dao;

import com.dianping.ba.base.framework.biz.dao.po.AccountPO;
import org.apache.ibatis.annotations.Param;

public interface AccountDao {
    AccountPO query(@Param("accountPO") AccountPO accountPO);
}
