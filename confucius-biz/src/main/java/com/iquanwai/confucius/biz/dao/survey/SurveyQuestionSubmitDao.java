package com.iquanwai.confucius.biz.dao.survey;

import com.iquanwai.confucius.biz.dao.DBUtil;
import com.iquanwai.confucius.biz.po.survey.SurveyQuestionSubmit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nethunder on 2017/1/17.
 */
public class SurveyQuestionSubmitDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public int insert(SurveyQuestionSubmit submit){
//        QueryRunner run = new QueryRunner(getDataSource());
//        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);
//        String insertSql = "INSERT INTO SurveyQuestionSubmit(ModuleId, ReferencedId , RemoteIp,  RealName, Length, Type, Thumbnail) " +
//                "VALUES (?, ?, ?, ?, ?, ?, ?)";
//        try{
//            Future<Integer> result = asyncRun.update(insertSql,
//                    .getModuleId(),picture.getReferencedId(),picture.getRemoteIp(),picture.getRealName(),picture.getLength(),picture.getType(),picture.getThumbnail());
//            return result.get();
//        } catch (SQLException e){
//            logger.error(e.getLocalizedMessage(), e);
//        } catch (InterruptedException e){
//            // ignore
//        } catch (ExecutionException e){
//            logger.error(e.getMessage(), e);
//        }
        return -1;
    }
}
