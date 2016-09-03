package com.iquanwai.confucius.web.course.controller;

import com.google.gson.Gson;
import com.iquanwai.confucius.biz.dao.po.*;
import com.iquanwai.confucius.biz.domain.course.progress.CourseStudyService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.resolver.LoginUser;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.course.dto.AnswerDto;
import com.iquanwai.confucius.web.course.dto.ChapterPageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 * Created by justin on 16/8/25.
 */
@Controller
@RequestMapping("/chapter")
public class ChapterController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private CourseStudyService courseStudyService;
    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping("/load/{chapterId}")
    public ResponseEntity<Map<String, Object>> load(LoginUser loginUser,
                                                    @PathVariable("chapterId") Integer chapterId){
        try{
            Assert.notNull(loginUser, "用户不能为空");
            ChapterPageDto chapterPageDto = loadPage(loginUser, chapterId, null);
            if(chapterPageDto==null){
                return WebUtils.error(200, "获取用户当前章节页失败");
            }
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("章节")
                    .function("学习章节")
                    .action("打开上次学习的章节页")
                    .memo(chapterId+","+chapterPageDto.getPage().getSequence());
            operationLogService.log(operationLog);
            return WebUtils.result(chapterPageDto);
        }catch (Exception e){
            LOGGER.error("获取用户当前章节页失败", e);
            return WebUtils.error(200, "获取用户当前章节页失败");
        }
    }

    private ChapterPageDto loadPage(LoginUser loginUser, int chapterId, Integer pageSequence) {
        ChapterPageDto chapterPageDto = new ChapterPageDto();
        Page page = courseStudyService.loadPage(loginUser.getOpenId(), chapterId, pageSequence);
        chapterPageDto.setPage(page);
        Chapter chapter = courseStudyService.loadChapter(loginUser.getOpenId(), chapterId);
        chapterPageDto.setChapterPic(chapter.getIcon());
        chapterPageDto.setChapterType(chapter.getType());
        chapterPageDto.setChapterName(chapter.getName());
        chapterPageDto.setChapterId(chapterId);
        chapterPageDto.setOpenid(loginUser.getOpenId());
        chapterPageDto.setUsername(loginUser.getWeixinName());

        return chapterPageDto;
    }

    @RequestMapping("/page/{chapterId}/{sequence}")
    public ResponseEntity<Map<String, Object>> load(LoginUser loginUser,
                                                    @PathVariable("chapterId") Integer chapterId,
                                                    @PathVariable("sequence") Integer pageSequence){
        try{
            Assert.notNull(loginUser, "用户不能为空");
            ChapterPageDto chapterPageDto = loadPage(loginUser, chapterId, pageSequence);
            if(chapterPageDto==null){
                return WebUtils.error(200, "获取用户当前章节页失败");
            }
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("章节")
                    .function("学习章节")
                    .action("打开章节某页")
                    .memo(chapterId+","+pageSequence);
            operationLogService.log(operationLog);
            return WebUtils.result(chapterPageDto);
        }catch (Exception e){
            LOGGER.error("获取用户当前章节页失败", e);
            return WebUtils.error(200, "获取用户当前章节页失败");
        }
    }

    @RequestMapping("/question/load/{questionId}")
    public ResponseEntity<Map<String, Object>> loadQuestion(LoginUser loginUser,
                                                            @PathVariable("questionId") Integer questionId){
        try{
            Assert.notNull(loginUser, "用户不能为空");
            Question question = courseStudyService.loadQuestion(loginUser.getOpenId(), questionId);
            if(question==null){
                return WebUtils.error(200, "获取选择题失败");
            }
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("章节")
                    .function("学习章节")
                    .action("加载选择题")
                    .memo(question.getId()+"");
            operationLogService.log(operationLog);
            return WebUtils.result(question);
        }catch (Exception e){
            LOGGER.error("获取选择题失败", e);
            return WebUtils.error(200, "获取选择题失败");
        }
    }

    @RequestMapping(value="/homework/submit/{questionId}", method= RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitQuestion(LoginUser loginUser,
                                                              @PathVariable("questionId") Integer questionId,
                                                              @RequestBody String body){
        try{
            Gson gson = new Gson();
            AnswerDto answerDto = gson.fromJson(body, AnswerDto.class);

            courseStudyService.submitQuestion(loginUser.getOpenId(), questionId, answerDto.getAnswers());

            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("章节")
                    .function("回答问题")
                    .action("提交问题")
                    .memo(questionId+"");
            operationLogService.log(operationLog);
            return WebUtils.success();
        }catch (Exception e){
            LOGGER.error("回答问题失败", e);
            return WebUtils.error(200, "回答问题失败");
        }
    }

    @RequestMapping("/homework/load/{homeworkId}")
    public ResponseEntity<Map<String, Object>> loadHomework(LoginUser loginUser,
                                                            @PathVariable("homeworkId") Integer homeworkId){
        try{
            Assert.notNull(loginUser, "用户不能为空");
            Homework homework = courseStudyService.loadHomework(loginUser.getOpenId(), homeworkId);
            if(homework==null){
                return WebUtils.error(200, "获取作业失败");
            }
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("作业")
                    .function("做作业")
                    .action("加载作业")
                    .memo(homework.getId()+"");
            operationLogService.log(operationLog);
            return WebUtils.result(homework);
        }catch (Exception e){
            LOGGER.error("获取作业失败", e);
            return WebUtils.error(200, "获取作业失败");
        }
    }

    @RequestMapping(value="/homework/submit/{homeworkId}", method= RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitHomework(LoginUser loginUser,
                                                      @PathVariable("homeworkId") Integer homeworkId,
                                                      @RequestBody String body){
        try{
            Assert.notNull(loginUser, "用户不能为空");
            courseStudyService.submitHomework(body, loginUser.getOpenId(), homeworkId);

            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("作业")
                    .function("做作业")
                    .action("提交作业")
                    .memo(homeworkId+"");
            operationLogService.log(operationLog);
            return WebUtils.success();
        }catch (Exception e){
            LOGGER.error("提交作业失败", e);
            return WebUtils.error(200, "提交作业失败");
        }
    }

    @RequestMapping(value="/complete/{chapterId}", method= RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> completeChapter(LoginUser loginUser,
                                                              @PathVariable("chapterId") Integer chapterId){
        try{
            Assert.notNull(loginUser, "用户不能为空");
            courseStudyService.completeChapter(loginUser.getOpenId(), chapterId);

            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("章节")
                    .function("章节完成")
                    .action("章节完成")
                    .memo(chapterId+"");
            operationLogService.log(operationLog);
            return WebUtils.success();
        }catch (Exception e){
            LOGGER.error("回答问题失败", e);
            return WebUtils.error(200, "回答问题失败");
        }
    }
}
