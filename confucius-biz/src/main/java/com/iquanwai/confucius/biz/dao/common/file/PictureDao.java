package com.iquanwai.confucius.biz.dao.common.file;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.Picture;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2016/12/15.
 */
@Repository
public class PictureDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<Picture> picture(Integer moduleId, Integer referId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Picture>> h = new BeanListHandler<>(Picture.class);
        try {
            List<Picture> pictureList = run.query("select * from Picture where ModuleId=? and ReferencedId=?", h, moduleId, referId);
            return pictureList;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public int upload(Picture picture) {
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "INSERT INTO Picture(ModuleId, ReferencedId , RemoteIp,  RealName, Length, Type, Thumbnail) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            return run.insert(insertSql, new ScalarHandler<Long>(),
                    picture.getModuleId(), picture.getReferencedId(), picture.getRemoteIp(), picture.getRealName(),
                    picture.getLength(), picture.getType(), picture.getThumbnail()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
