package com.iquanwai.confucius.biz.dao.permission;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.permisson.Permission;
import com.iquanwai.confucius.biz.po.permisson.Role;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2016/12/29.
 */
@Repository
public class RoleDao extends DBUtil{

    private Logger logger = LoggerFactory.getLogger(getClass());

}
