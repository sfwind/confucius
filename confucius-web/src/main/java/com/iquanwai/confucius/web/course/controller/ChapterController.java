package com.iquanwai.confucius.web.course.controller;

import com.iquanwai.confucius.biz.dao.po.*;
import com.iquanwai.confucius.biz.domain.course.progress.CourseStudyService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.resolver.LoginUser;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.course.dto.ChapterPageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
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
        Chapter chapter = courseStudyService.loadChapter(chapterId);
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
    public ResponseEntity<Map<String, Object>> loadQuestion(LoginUser loginUser){
        Question question = new Question();
        question.setId(1);
        question.setMaterialId(5);
        question.setSubject("问题1-blabla");
        question.setPoint(100);
        question.setAnalysis("问题解析问题解析问题解析问题解析");
        List<Choice> choiceList = new ArrayList<Choice>();
        question.setChoiceList(choiceList);
        Choice choice1 = new Choice();
        choice1.setSubject("选项1");
        choice1.setId(1);
        choice1.setSequence(1);
        choice1.setRight(false);
        choice1.setQuestionId(1);
        choiceList.add(choice1);
        Choice choice2 = new Choice();
        choice2.setSubject("选项2");
        choice2.setId(2);
        choice2.setSequence(2);
        choice2.setRight(true);
        choice2.setQuestionId(1);
        choiceList.add(choice2);
        Choice choice3 = new Choice();
        choice3.setSubject("选项3");
        choice3.setId(3);
        choice3.setSequence(3);
        choice3.setRight(true);
        choice3.setQuestionId(1);
        choiceList.add(choice3);
        return WebUtils.result(question);
    }

    @RequestMapping("/homework/load/{homeworkId}")
    public ResponseEntity<Map<String, Object>> loadHomework(LoginUser loginUser){
        Homework homework = new Homework();
        homework.setId(1);
        homework.setMaterialId(5);
        homework.setSubject("问题1-blabla");
        homework.setPoint(100);

        return WebUtils.result(homework);
    }
}
