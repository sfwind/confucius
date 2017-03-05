package com.iquanwai.confucius.biz.dao.fragmentation;

import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.ArticleViewInfo;
import com.iquanwai.confucius.biz.po.fragmentation.FragmentDailyData;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/2/27.
 */
@Component
public class FragmentAnalysisDataDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public FragmentDailyData getDailyData(){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<FragmentDailyData> h = new BeanHandler<FragmentDailyData>(FragmentDailyData.class);
        try{
            return run.query("select t1.count ProblemCount,t2.rate DailyExpiredWarmCompleteRate,warm.total WarmCompleteCount , " +
                            "warmRight.total/warm.total WarmRightRate, " +
                            "warmComment.count WarmCommentCount,challengeSubmit.count ChallengeSubmitCount, " +
                            "challengeShow.count ChallengeShowCount,challengeComment.count ChallengeCommentCount,challengeVote.count ChallengeVoteCount, " +
                            "applicationSubmit.count ApplicationSubmitCount,applicationSubmit.count/appTotal.count ApplicationCompleteRate, " +
                            "appShow.count ApplicationShowCount,appComment.count ApplicationCommentCount,appVote.count ApplicationVoteCount " +
                            "from " +
                            "  (SELECT count(1) count " +
                            "   FROM " +
                            "     fragmentCourse.ImprovementPlan " +
                            "  ) t1 " +
                            "left join ( " +
                            "  select sum(WarmupComplete),sum(totalPractice),sum(WarmupComplete)/sum(totalPractice) as rate from ( " +
                            "    SELECT " +
                            "      a.Id, " +
                            "      a.WarmupComplete, " +
                            "      a.CloseDate, " +
                            "      count(1) AS totalPractice " +
                            "    FROM fragmentCourse.ImprovementPlan a " +
                            "      LEFT JOIN fragmentCourse.PracticePlan b ON a.Id = b.PlanId AND b.Type = 1 " +
                            "    WHERE CloseDate = Date('2017-02-23') " +
                            "    GROUP BY a.Id " +
                            "  ) t)t2 on 1=1 " +
                            "left join ( " +
                            "    select sum(count) total from ( " +
                            "      SELECT " +
                            "        PlanId, " +
                            "        cast(count(1) / 3 AS SIGNED) AS count " +
                            "      FROM fragmentCourse.WarmupSubmit " +
                            "      GROUP BY PlanId " +
                            "    )t) warm on 1=1 " +
                            "left join ( " +
                            "    select sum(count) total from ( " +
                            "    SELECT " +
                            "      PlanId, " +
                            "      cast(count(1) / 3 AS SIGNED) AS count, " +
                            "      Date(AddTime)                AS date " +
                            "    FROM fragmentCourse.WarmupSubmit " +
                            "    WHERE IsRight = 1 " +
                            "    GROUP BY PlanId, Date(AddTime) " +
                            "    ) t) warmRight on 1=1 " +
                            "left join ( " +
                            "    SELECT count(1) count " +
                            "    FROM fragmentCourse.WarmupPracticeDiscuss " +
                            "    ) warmComment on 1=1 " +
                            "left join ( " +
                            "    SELECT count(1) count " +
                            "    FROM fragmentCourse.ChallengeSubmit " +
                            "    WHERE Length(Content) > 1 " +
                            "    ) challengeSubmit on 1=1 " +
                            "left join ( " +
                            "    SELECT " +
                            "      count(1) count " +
                            "    FROM quanwai.OperatingLog " +
                            "    WHERE Action = 'PC查看挑战任务' " +
                            "    ) challengeShow on 1=1 " +
                            "left join ( " +
                            "    SELECT " +
                            "      count(1)      count " +
                            "    FROM fragmentCourse.Comment " +
                            "    WHERE ModuleId = 1 " +
                            "    ) challengeComment on 1=1 " +
                            "left join ( " +
                            "    SELECT " +
                            "      count(1)      count " +
                            "    FROM fragmentCourse.HomeworkVote " +
                            "    WHERE Type = 1 " +
                            "    ) challengeVote on 1=1 " +
                            "left join ( " +
                            "    SELECT count(1) count " +
                            "    FROM fragmentCourse.ApplicationSubmit " +
                            "    WHERE Content IS NOT NULL " +
                            "    ) applicationSubmit on 1=1 " +
                            "left join ( " +
                            "    SELECT " +
                            "      count(1)      count " +
                            "    FROM fragmentCourse.PracticePlan " +
                            "    WHERE Type = 11 " +
                            "    ) appTotal on 1=1 " +
                            "left join ( " +
                            "    SELECT " +
                            "      count(1)      count " +
                            "    FROM quanwai.OperatingLog " +
                            "    WHERE Action = 'PC查看应用任务内容' " +
                            "    ) appShow on 1=1 " +
                            "left join ( " +
                            "    SELECT " +
                            "      count(1)      count " +
                            "    FROM fragmentCourse.Comment " +
                            "    WHERE ModuleId = 2 " +
                            "    ) appComment on 1=1 " +
                            "left join ( " +
                            "    SELECT " +
                            "      count(1)      count " +
                            "    FROM fragmentCourse.HomeworkVote " +
                            "    WHERE Type = 2 " +
                            "    ) appVote on 1=1; "
            ,h);
        } catch (SQLException e){
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public long insertDailyData(FragmentDailyData data){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO FragmentDailyData(ProblemCount, " +
                "DailyExpiredWarmCompleteRate, WarmCompleteCount, " +
                "WarmRightRate, WarmCommentCount, ChallengeSubmitCount, " +
                "ChallengeShowCount, ChallengeCommentCount, ChallengeVoteCount, " +
                "ApplicationSubmitCount, ApplicationCompleteRate, ApplicationShowCount, " +
                "ApplicationCommentCount, ApplicationVoteCount) " +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    data.getProblemCount(), data.getDailyExpiredWarmCompleteRate(),data.getWarmCompleteCount(),
                    data.getWarmRightRate(),data.getWarmCommentCount(),
                    data.getChallengeSubmitCount(),data.getChallengeShowCount(),data.getChallengeCommentCount(),data.getChallengeVoteCount(),
                    data.getApplicationSubmitCount(),data.getApplicationCompleteRate(),data.getApplicationShowCount(),
                    data.getApplicationCommentCount(),data.getApplicationVoteCount());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1L;

    }

    public long insertArticleViewInfo(ArticleViewInfo info){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO ArticleViewInfo(ArticleType,ArticleId) VALUE(?,?)";
        try{
            Long insertRs = runner.insert(sql, new ScalarHandler<>(), info.getArticleType(), info.getArticleId());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1L;
    }

    public int riseArticleViewCount(Integer articleType, Integer articleId) {
        QueryRunner runner = new QueryRunner((getDataSource()));
        String sql = "UPDATE ArticleViewInfo SET Count = Count + 1 where ArticleId = ? and ArticleType = ?";
        try{
            return runner.update(sql, articleId, articleType);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
