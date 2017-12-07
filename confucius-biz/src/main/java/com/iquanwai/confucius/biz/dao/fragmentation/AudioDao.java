package com.iquanwai.confucius.biz.dao.fragmentation;

import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.Audio;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by 三十文 on 2017/9/22
 */
@Repository
public class AudioDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insertAudio(Audio audio) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO Audio (Name, Url, Words) VALUES (?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    audio.getName(),
                    audio.getUrl(),
                    audio.getWords());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 修改带文件路径的Audio
     * @param audio
     */
    public void updateAudioContainsUrl(Audio audio) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update Audio set Name=?, Url=?, Words=? where id=?";
        try {
            runner.update(sql,
                    audio.getName(),
                    audio.getUrl(),
                    audio.getWords(),
                    audio.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * 修改不带文件路径的Audio
     * @param audio
     */
    public void updateAudio(Audio audio){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update Audio set Name = ?,Words = ? where id = ?";
        try {
            runner.update(sql,audio.getName(),audio.getWords(),audio.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
    }

}
