package com.iquanwai.confucius.biz.dao.asst;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.asst.AsstUpDefault;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class AsstUpDefaultDao  extends DBUtil{

    private Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 根据教练级别加载默认的助教升级标准
     * @param roleId
     * @return
     */
    public AsstUpDefault queryByRoleId(Integer roleId){
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<AsstUpDefault> h = new BeanHandler<AsstUpDefault>(AsstUpDefault.class);
        String sql = " SELECT * FROM AsstUpDefault WHERE RoleId = ? AND DEL = 0 ";

        try {
           return runner.query(sql,h,roleId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return null;
    }
}
