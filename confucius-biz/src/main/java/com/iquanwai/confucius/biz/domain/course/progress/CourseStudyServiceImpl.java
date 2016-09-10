package com.iquanwai.confucius.biz.domain.course.progress;

import com.iquanwai.confucius.biz.dao.course.*;
import com.iquanwai.confucius.biz.po.*;
import com.iquanwai.confucius.biz.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by justin on 16/8/31.
 */
@Service
public class CourseStudyServiceImpl implements CourseStudyService {
    @Autowired
    private PageDao pageDao;
    @Autowired
    private CurrentChapterPageDao currentChapterPageDao;
    @Autowired
    private MaterialDao materialDao;
    @Autowired
    private ChapterDao chapterDao;
    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private ChoiceDao choiceDao;
    @Autowired
    private QuestionSubmitDao questionSubmitDao;
    @Autowired
    private HomeworkDao homeworkDao;
    @Autowired
    private HomeworkSubmitDao homeworkSubmitDao;
    @Autowired
    private ClassMemberDao classMemberDao;


    public Page loadPage(String openid, int chapterId, Integer pageSequence) {
        Assert.notNull(openid, "openid不能为空");
        if(pageSequence==null){
            pageSequence = currentChapterPageDao.currentPage(openid, chapterId);
        }

        //首次学习
        if(pageSequence==null){
            pageSequence = 1;
        }

        Page page = pageDao.loadPage(chapterId, pageSequence);
        if(page!=null) {
            List<Material> materialList = materialDao.loadPageMaterials(page.getId());
            page.setMaterialList(materialList);
            //记录到阅读到第几页
            currentChapterPageDao.updatePage(openid, chapterId, pageSequence);
        }
        return page;
    }

    public Chapter loadChapter(String openid, int chapterId) {
        Assert.notNull(openid, "openid不能为空");
        ClassMember classMember = classMemberDao.activeCourse(openid);
        Chapter chapter = chapterDao.load(Chapter.class, chapterId);
        String progress = "";
        boolean mark = false;
        if(StringUtils.isEmpty(classMember.getProgress())){
            progress = chapterId +"";
        }else{
            String[] progressArr = classMember.getProgress().split(",");
            for(String prog:progressArr){
                if(prog.equals(String.valueOf(chapterId))){
                    mark = true;
                }
            }
            if(!mark) {
                progress = progress+","+chapterId;
            }
        }
        if(!mark) {
            classMemberDao.progress(openid, chapterId, progress);
        }
        // TODO:报过班才能看
//        if(chapter.getCourseId().equals(classMember.getCourseId())){
//            return chapter;
//        }else{
//            return null;
//        }

        return chapter;
    }

    public Question loadQuestion(String openid, int questionId) {
        Assert.notNull(openid, "openid不能为空");
        ClassMember classMember = classMemberDao.activeCourse(openid);
        Question question = questionDao.load(Question.class, questionId);
        if(question!=null){
            boolean submitted = questionSubmitDao.submitted(openid, classMember.getClassId(), questionId);
            question.setAnswered(submitted);
            List<Choice> choiceList = choiceDao.loadChoices(questionId);
            question.setChoiceList(choiceList);
        }
        return question;
    }

    public Homework loadHomework(String openid, int homeworkId) {
        Assert.notNull(openid, "openid不能为空");
        ClassMember classMember = classMemberDao.activeCourse(openid);
        Homework homework = homeworkDao.load(Homework.class, homeworkId);
        if(homework!=null){
            HomeworkSubmit submit = homeworkSubmitDao.loadHomeworkSubmit(openid, classMember.getClassId(), homeworkId);
            if(submit==null || submit.getSubmitContent()==null) {
                homework.setSubmitted(false);
            }else{
                homework.setSubmitted(true);
            }
            if(submit==null){
                String url = "/homework/load/"+ CommonUtils.randomString(6);
                homeworkSubmitDao.insert(openid, classMember.getClassId(), homeworkId, url);
            }
        }
        return homework;
    }

    public HomeworkSubmit loadHomework(String url) {
        return homeworkSubmitDao.loadByUrl(url);
    }

    public void submitHomework(String content, String openid, Integer homeworkId) {
        Assert.notNull(openid, "openid不能为空");
        ClassMember classMember = classMemberDao.activeCourse(openid);
        homeworkSubmitDao.submit(homeworkId, classMember.getClassId(), openid, content);
    }

    public void submitQuestion(String openid, Integer questionId, List<Integer> choiceList) {
        Assert.notNull(openid, "openid不能为空");
        String answer = "";
        ClassMember classMember = classMemberDao.activeCourse(openid);
        Integer score = score(questionId, choiceList);
        for(Integer choice:choiceList){
            answer = answer+","+choice;
        }
        answer = answer.substring(1);
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setClassId(classMember.getClassId());
        questionSubmit.setScore(score);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setSubmitAnswer(answer);
        questionSubmit.setSubmitOpenid(openid);
        questionSubmitDao.insert(questionSubmit);
    }

    public void completeChapter(String openid, Integer chapterId) {
//        ClassMember classMember = classMemberDao.activeCourse(openid);
//        String progress = "";
//        if(StringUtils.isEmpty(classMember.getProgress())){
//            progress = chapterId +"";
//        }else{
//            progress = progress + ","+ chapterId;
//        }
//        classMemberDao.progress(openid, classMember.getClassId(), progress);
    }

    private Integer score(Integer questionId, List<Integer> choiceList) {
        Assert.notNull(choiceList, "选项不能为空");
        List<Choice> right = choiceDao.loadRightChoices(questionId);

        for(Choice choice:right){
            if(!choiceList.contains(choice.getId())) {
                return 0;
            }
        }

        Question question = questionDao.load(Question.class, questionId);
        if(right.size()==choiceList.size()){
            return question.getPoint();
        }
        return 0;
    }
}
