package com.iquanwai.confucius.biz.dao.course;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.Material;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 16/9/3.
 */
@Repository
public class MaterialDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<Material> loadPageMaterials(int pageId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Material>> h = new BeanListHandler(Material.class);

        try {
            List<Material> materialList = run.query("SELECT * FROM Material where PageId=? AND DEL = 0 order by Sequence",
                    h, pageId);
            return materialList;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public List<Material> loadPageMaterials(List<Integer> pageIdList){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Material>> h = new BeanListHandler(Material.class);

        String questionMark = produceQuestionMark(pageIdList.size());
        try {
            List<Material> materialList = run.query("SELECT * FROM Material where PageId in ("+questionMark+") AND DEL = 0",
                    h, pageIdList.toArray());
            return materialList;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }
}
