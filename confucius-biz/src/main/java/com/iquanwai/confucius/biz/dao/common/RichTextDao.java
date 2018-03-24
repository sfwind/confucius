package com.iquanwai.confucius.biz.dao.common;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.RichText;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by 三十文
 */
@Repository
public class RichTextDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insertRichText(RichText richText) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO RichText (Title, Content, Uuid) VALUES (?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(), richText.getTitle(), richText.getContent(), richText.getUuid());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

}
