package com.iquanwai.confucius.web.course.controller;

import com.iquanwai.confucius.biz.dao.po.*;
import com.iquanwai.confucius.resolver.LoginUser;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.course.dto.ChapterPageDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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

    //load完之前，记录用户阅读到的页
    @RequestMapping("/load/{chapterId}")
    public ResponseEntity<Map<String, Object>> load(LoginUser loginUser){
        ChapterPageDto chapterPageDto = new ChapterPageDto();
        chapterPageDto.setDone(false);
        chapterPageDto.setOpenid("oK881wQekezGpw6rq790y_vAY_YY");
        chapterPageDto.setUsername("风之伤");
        chapterPageDto.setChapterId(1);
        chapterPageDto.setChapterName("结构化思维（1）");
        chapterPageDto.setChapterPic("http://someurl");
        Page page = new Page();
        chapterPageDto.setPage(page);
        page.setId(1);
        page.setSequence(1);
        List<Material> materialList = new ArrayList<Material>();
        page.setMaterialList(materialList);
        Material material1 = new Material();
        material1.setType(1);
        material1.setSequence(1);
        material1.setId(1);
        material1.setContent("文章正文文章正文文章正文文章正文文章正文文章正文");
        material1.setPageId(1);
        materialList.add(material1);

        Material material2 = new Material();
        material2.setType(2);
        material2.setSequence(2);
        material2.setId(2);
        material2.setContent("http://someurl");
        material2.setPageId(1);
        materialList.add(material2);

        Material material3 = new Material();
        material3.setType(3);
        material3.setSequence(3);
        material3.setId(3);
        material3.setContent("http://someurl");
        material3.setPageId(1);
        materialList.add(material3);

        Material material4 = new Material();
        material4.setType(4);
        material4.setSequence(4);
        material4.setId(4);
        material4.setContent("作业题干作业题干作业题干作业题干作业题干作业题干");
        material4.setPageId(1);
        materialList.add(material4);

        Material material5 = new Material();
        material5.setType(5);
        material5.setSequence(5);
        material5.setId(5);
        material5.setPageId(1);

        materialList.add(material5);


        return WebUtils.result(chapterPageDto);
    }

    //load完之前，记录用户阅读到的页
    @RequestMapping("/page/{chapterId}/{sequence}")
    public ResponseEntity<Map<String, Object>> page(LoginUser loginUser){
        ChapterPageDto chapterPageDto = new ChapterPageDto();
        chapterPageDto.setDone(false);
        chapterPageDto.setOpenid("oK881wQekezGpw6rq790y_vAY_YY");
        chapterPageDto.setUsername("风之伤");
        chapterPageDto.setChapterId(1);
        chapterPageDto.setChapterName("结构化思维（1）");
        chapterPageDto.setChapterPic("http://someurl");
        Page page = new Page();
        chapterPageDto.setPage(page);
        page.setId(1);
        page.setSequence(1);
        List<Material> materialList = new ArrayList<Material>();
        page.setMaterialList(materialList);
        Material material1 = new Material();
        material1.setType(1);
        material1.setSequence(1);
        material1.setId(1);
        material1.setContent("文章正文文章正文文章正文文章正文文章正文文章正文");
        material1.setPageId(1);
        materialList.add(material1);

        Material material2 = new Material();
        material2.setType(2);
        material2.setSequence(2);
        material2.setId(2);
        material2.setContent("http://someurl");
        material2.setPageId(1);
        materialList.add(material2);

        Material material3 = new Material();
        material3.setType(3);
        material3.setSequence(3);
        material3.setId(3);
        material3.setContent("http://someurl");
        material3.setPageId(1);
        materialList.add(material3);

        Material material4 = new Material();
        material4.setType(4);
        material4.setSequence(4);
        material4.setId(4);
        material4.setContent("作业题干作业题干作业题干作业题干作业题干作业题干");
        material4.setPageId(1);
        materialList.add(material4);

        Material material5 = new Material();
        material5.setType(5);
        material5.setSequence(5);
        material5.setId(5);
        material5.setPageId(1);
        materialList.add(material5);


        return WebUtils.result(chapterPageDto);
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
