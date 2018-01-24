package com.iquanwai.confucius.biz.domain.asst;

import com.iquanwai.confucius.biz.domain.fragmentation.practice.RiseWorkInfoDto;
import com.iquanwai.confucius.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.confucius.biz.po.apply.InterviewRecord;
import com.iquanwai.confucius.biz.po.asst.AsstUpExecution;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import com.iquanwai.confucius.biz.util.page.Page;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 17/4/26.
 */
public interface AssistantCoachService {

    /**
     * 获取助教的评论数
     * @param profileId 助教id
     * @return 当日评论数/总评论数
     * */
    Pair<Integer, Integer> getCommentCount(Integer profileId);


    /**
     * 获取待评论的小课分享
     * @param problemId 课程id
     * */
    List<RiseWorkInfoDto> getUnderCommentArticles(Integer problemId);


    /**
     * 获取待评论的应用练习
     * @param problemId 课程id
     * */
    List<RiseWorkInfoDto> getUnderCommentApplications(Integer problemId);

    /**
     * 模糊查询所有应用练习
     */
    List<RiseWorkInfoDto> getUnderCommentApplicationsByNickName(Integer problemId, String nickName);

    List<RiseWorkInfoDto> getUnderCommentApplicationsByMemberId(Integer problemId, String memberId);

    List<RiseWorkInfoDto> getUnderCommentApplicationsByClassNameAndGroup(Integer problemId,String className,String groupId);


    /**
     * 获取待评论的应用练习数量
     * */
    Map<Integer, Integer> getUnderCommentApplicationCount();

    /**
     * 获取待评论的小课分享数量
     * */
    Map<Integer, Integer> getUnderCommentSubjectArticleCount();

    /**
     * 获取助教已评论的文章
     * @param profileId 助教id
     * */
    List<RiseWorkInfoDto> getCommentedSubmit(Integer profileId);


    /**
     * 获取RiseClassMember中的ClassName和GroupId
     * @return
     */
    List<RiseClassMember> loadClassNameAndGroupId();

    /**
     * 加载教练
     * @return
     */
    List<UserRole> loadAssists();

    /**
     * 对教练进行升降级
     */

    Integer updateAssist(Integer id,Integer roleId);

    /**
     * 教练过期
     */

    Integer deleteAssist(Integer id);

    /**
     * 根据NickName加载非教练人员
     * @param nickName
     * @return
     */
    List<Profile> loadUnAssistByNickName(String nickName);

    /**
     * 添加教练
     * @param roleId
     * @param riseId
     * @return
     */
    Integer addAssist(Integer roleId,String riseId);

    /**
     * 加载教练审核中的商学院申请
     * @param interviewer
     * @return
     */
    List<BusinessSchoolApplication> loadByInterviewer(Integer interviewer,Page page);

    /**
     * 加载审批的面试记录
     * @param applyId
     * @return
     */
    InterviewRecord loadInterviewRecord(Integer applyId);

    /**
     * 添加或者更新审批记录
     */
    Integer addInterviewRecord(InterviewRecord interviewRecord);

    /**
     * 获取教练被的分配的数量
     * @param interviewer
     * @return
     */
    Integer loadAssignedCount(Integer interviewer);
}
