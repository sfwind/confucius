package com.iquanwai.confucius.web.course.controller;

import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.domain.course.progress.CourseStudyService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.*;
import com.iquanwai.confucius.biz.po.systematism.*;
import com.iquanwai.confucius.biz.util.ErrorMessageUtils;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import com.iquanwai.confucius.web.course.dto.AnswerDto;
import com.iquanwai.confucius.web.course.dto.ChapterPageDto;
import com.iquanwai.confucius.web.course.dto.HomeworkSubmitDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by justin on 16/8/25.
 */
@RestController
@RequestMapping("/chapter")
public class ChapterController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private CourseStudyService courseStudyService;
    @Autowired
    private CourseProgressService courseProgressService;
    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping("/load/{chapterId}")
    public ResponseEntity<Map<String, Object>> load(LoginUser loginUser,
                                                    @PathVariable("chapterId") Integer chapterId){
        Assert.notNull(loginUser, "用户不能为空");
        Chapter chapter = courseStudyService.loadChapter(loginUser.getOpenId(), chapterId);
        if(chapter==null){
            return WebUtils.error("获取用户当前章节页失败");
        }
        ClassMember classMember = courseProgressService.loadActiveCourse(loginUser.getOpenId(), chapter.getCourseId());
        if(classMember==null){
            LOGGER.error("用户"+loginUser.getWeixinName()+"还没有报名, openid is {}", loginUser.getOpenId());
            return WebUtils.error(ErrorMessageUtils.getErrmsg("course.load.nopaid"));
        }
        ChapterPageDto chapterPageDto = loadPage(loginUser, chapter, null, false);
        if(chapterPageDto==null){
            return WebUtils.error("获取用户当前章节页失败");
        }

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("章节")
                .function("学习章节")
                .action("打开上次学习的章节页")
                .memo(chapterId+","+chapterPageDto.getPage().getSequence());
        operationLogService.log(operationLog);
        return WebUtils.result(chapterPageDto);
    }

    private ChapterPageDto loadPage(LoginUser loginUser, Chapter chapter, Integer pageSequence, Boolean lazyLoad) {
        ChapterPageDto chapterPageDto = new ChapterPageDto();
        Page page = courseStudyService.loadPage(loginUser.getOpenId(), chapter.getId(), pageSequence, lazyLoad);
        chapterPageDto.setPage(page);

        chapterPageDto.setChapterPic(chapter.getIcon());
        chapterPageDto.setChapterType(chapter.getType());
        chapterPageDto.setChapterName(chapter.getName());
        chapterPageDto.setChapterId(chapter.getId());
        chapterPageDto.setTotalPage(chapter.getTotalPage());

        return chapterPageDto;
    }

    @RequestMapping("/page/{chapterId}/{sequence}")
    public ResponseEntity<Map<String, Object>> load(LoginUser loginUser,
                                                    @PathVariable("chapterId") Integer chapterId,
                                                    @PathVariable("sequence") Integer pageSequence){
        Assert.notNull(loginUser, "用户不能为空");
        Chapter chapter = courseStudyService.loadChapter(loginUser.getOpenId(), chapterId);
        if(chapter==null){
            return WebUtils.error("获取用户当前章节页失败");
        }
        ClassMember classMember = courseProgressService.loadActiveCourse(loginUser.getOpenId(), chapter.getCourseId());
        if(classMember==null){
            LOGGER.error("用户"+loginUser.getWeixinName()+"还没有报名, openid is {}", loginUser.getOpenId());
            return WebUtils.error(ErrorMessageUtils.getErrmsg("course.load.nopaid"));
        }
        ChapterPageDto chapterPageDto = loadPage(loginUser, chapter, pageSequence, false);
        if(chapterPageDto==null){
            return WebUtils.error("获取用户当前章节页失败");
        }
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("章节")
                .function("学习章节")
                .action("打开章节某页")
                .memo(chapterId+","+pageSequence);
        operationLogService.log(operationLog);
        return WebUtils.result(chapterPageDto);
    }

    @RequestMapping("/page/lazyLoad/{chapterId}/{sequence}")
    public ResponseEntity<Map<String, Object>> lazyLoad(LoginUser loginUser,
                                                        @PathVariable("chapterId") Integer chapterId,
                                                        @PathVariable("sequence") Integer pageSequence){
        Assert.notNull(loginUser, "用户不能为空");
//            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
//                    .module("章节")
//                    .function("学习章节")
//                    .action("打开章节某页")
//                    .memo(chapterId+","+pageSequence);
//            operationLogService.log(operationLog);
        Chapter chapter = courseStudyService.loadChapter(loginUser.getOpenId(), chapterId);
        if(chapter==null){
            return WebUtils.error("懒加载章节页失败");
        }
        ChapterPageDto chapterPageDto = loadPage(loginUser, chapter, pageSequence, true);
        if(chapterPageDto==null){
            return WebUtils.error("懒加载章节页失败");
        }
        return WebUtils.result(chapterPageDto);
    }

    @RequestMapping("/question/load/{questionId}")
    public ResponseEntity<Map<String, Object>> loadQuestion(LoginUser loginUser,
                                                            @PathVariable("questionId") Integer questionId){
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("章节")
                .function("学习章节")
                .action("加载选择题")
                .memo(questionId+"");
        operationLogService.log(operationLog);
        Question question = courseStudyService.loadQuestion(loginUser.getOpenId(), questionId);
        if(question==null){
            return WebUtils.error("获取选择题失败");
        }
        return WebUtils.result(question);
    }

    @RequestMapping(value="/answer/{questionId}", method= RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitQuestion(LoginUser loginUser,
                                                              @PathVariable("questionId") Integer questionId,
                                                              @RequestBody AnswerDto answerDto){
        Assert.notNull(loginUser, "用户不能为空");
        boolean right = courseStudyService.submitQuestion(loginUser.getOpenId(), questionId, answerDto.getAnswers());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("章节")
                .function("回答问题")
                .action("提交问题")
                .memo(questionId+":"+right);
        operationLogService.log(operationLog);
        return WebUtils.result(right);
    }

    @RequestMapping("/homework/load/{homeworkId}")
    public ResponseEntity<Map<String, Object>> loadHomework(LoginUser loginUser,
                                                            @PathVariable("homeworkId") Integer homeworkId){
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("作业")
                .function("做作业")
                .action("加载作业")
                .memo(homeworkId+"");
        operationLogService.log(operationLog);
        Homework homework = courseStudyService.loadHomework(loginUser.getOpenId(), homeworkId);
        if(homework==null){
            return WebUtils.error("获取作业失败");
        }
        return WebUtils.result(homework);
    }

    @RequestMapping(value="/homework/submit/{homeworkId}", method= RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitHomework(LoginUser loginUser,
                                                              @PathVariable("homeworkId") Integer homeworkId,
                                                              @RequestBody HomeworkSubmitDto homeworkSubmitDto){
        Assert.notNull(loginUser, "用户不能为空");
        if(StringUtils.isEmpty(homeworkSubmitDto.getAnswer())){
            return WebUtils.error("请写完后再提交");
        }
        if(homeworkSubmitDto.getAnswer().length()>10000){
            return WebUtils.error("字数太长，请删减到10000字以下");
        }
        courseStudyService.submitHomework(homeworkSubmitDto.getAnswer(), loginUser.getOpenId(), homeworkId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("作业")
                .function("做作业")
                .action("提交作业")
                .memo(homeworkId+"");
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value="/mark/page/{chapterId}/{sequence}", method= RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> markPage(LoginUser loginUser,
                                                        @PathVariable("chapterId") Integer chapterId,
                                                        @PathVariable("sequence") Integer sequence){
        Assert.notNull(loginUser, "用户不能为空");
        courseStudyService.markPage(loginUser.getOpenId(), chapterId, sequence);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("章节")
                .function("记录看到的页数")
                .action("记录看到的页数")
                .memo(chapterId+","+sequence);
        operationLogService.log(operationLog);
        return WebUtils.success();
    }
}
