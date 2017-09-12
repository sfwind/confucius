package com.iquanwai.confucius.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampSchedule;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by 三十文 on 2017/9/12
 */
@Repository
public class MonthlyCampScheduleDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<MonthlyCampSchedule> loadByMonth(Integer month) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM MonthlyCampSchedule WHERE Month = ?";
        ResultSetHandler<List<MonthlyCampSchedule>> h = new BeanListHandler<>(MonthlyCampSchedule.class);

        try {
            return runner.query(sql, h, month);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }


}
