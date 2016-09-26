package com.iquanwai.confucius.biz.domain.course.progress;

import com.iquanwai.confucius.biz.dao.course.*;
import com.iquanwai.confucius.biz.po.*;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
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


    public Page loadPage(String openid, int chapterId, Integer pageSequence, Boolean lazyLoad) {
        Assert.notNull(openid, "openid不能为空");

        //首次学习
        if(pageSequence==null){
            pageSequence = 1;
        }

        Page page = pageDao.loadPage(chapterId, pageSequence);
        if(page!=null) {
            List<Material> materialList = materialDao.loadPageMaterials(page.getId());
            page.setMaterialList(materialList);
            //记录到阅读到第几页
            if(!lazyLoad) {
                markPage(openid, chapterId, pageSequence);
            }
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
                homework.setPcurl(ConfigUtils.domainName()+url);
                homeworkSubmitDao.insert(openid, classMember.getClassId(), homeworkId, url);
            }else{
                if(submit.getSubmitUrl()!=null){
                    homework.setPcurl(ConfigUtils.domainName()+submit.getSubmitUrl());
                }
            }
        }
        return homework;
    }

    public HomeworkSubmit loadHomework(String url) {
        return homeworkSubmitDao.loadByUrl(url);
    }

    public List<HomeworkSubmit> loadSubmittedHomework(Integer homeworkId) {
        return homeworkSubmitDao.submittedHomework(homeworkId);
    }

    public void submitHomework(String content, String openid, Integer homeworkId) {
        Assert.notNull(openid, "openid不能为空");
        ClassMember classMember = classMemberDao.activeCourse(openid);
        homeworkSubmitDao.submit(homeworkId, classMember.getClassId(), openid, content);
    }

    public boolean submitQuestion(String openid, Integer questionId, List<Integer> choiceList) {
        Assert.notNull(openid, "openid不能为空");
        String answer = "";
        ClassMember classMember = classMemberDao.activeCourse(openid);
        Question q = questionDao.load(Question.class, questionId);
        Integer score = score(q, choiceList);
        boolean right = false;
        if(score.equals(q.getPoint())){
            right = true;
        }
        for(Integer choice:choiceList){
            answer = answer+","+choice;
        }

        if(StringUtils.isNotEmpty(answer)) {
            answer = answer.substring(1);
            QuestionSubmit questionSubmit = new QuestionSubmit();
            questionSubmit.setClassId(classMember.getClassId());
            questionSubmit.setScore(score);
            questionSubmit.setQuestionId(questionId);
            questionSubmit.setSubmitAnswer(answer);
            questionSubmit.setSubmitOpenid(openid);
            questionSubmitDao.insert(questionSubmit);
        }
        return right;
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

    public void remark(String openid, Integer classId, Integer homeworkId, boolean excellent, boolean fail) {
        int score = getScore(excellent, fail);
        homeworkSubmitDao.remark(homeworkId, classId, openid, null, score);
    }

    public void markPage(String openid, Integer chapterId, Integer pageSequence) {
        currentChapterPageDao.updatePage(openid, chapterId, pageSequence);
    }

    private int getScore(boolean excellent, boolean fail) {
        int score = 75;
        if(excellent){
            score = 90;
        }
        if(fail){
            score = 59;
        }
        return score;
    }

    private Integer score(Question question, List<Integer> choiceList) {
        Assert.notNull(choiceList, "选项不能为空");
        List<Choice> right = choiceDao.loadRightChoices(question.getId());

        for(Choice choice:right){
            if(!choiceList.contains(choice.getId())) {
                return 0;
            }
        }

        if(right.size()==choiceList.size()){
            return question.getPoint();
        }
        return 0;
    }
}
