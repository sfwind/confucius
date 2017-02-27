package com.iquanwai.confucius.biz.dao.fragmentation;

import com.iquanwai.confucius.biz.dao.PracticeDBUtil;
import com.iquanwai.confucius.biz.po.fragmentation.FragmentDailyData;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
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
                            "challengeShow.count ChallengeShowCount,challengeComment.count ChallengeCommentCount,challengeVote.count 专题点赞, " +
                            "applicationSubmit.count 应用训练提交总数,applicationSubmit.count/appTotal.count 应用累计完成率, " +
                            "appShow.count 应用训练总浏览量,appComment.count 应用评论总数,appVote.count 应用点赞 " +
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
}
