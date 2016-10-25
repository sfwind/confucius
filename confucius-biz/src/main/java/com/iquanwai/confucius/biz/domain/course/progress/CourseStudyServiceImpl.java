package com.iquanwai.confucius.biz.domain.course.progress;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iquanwai.confucius.biz.dao.course.*;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.*;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private CourseWeekDao courseWeekDao;
    @Autowired
    private RestfulHelper restfulHelper;
    @Autowired
    private ClassDao classDao;
    @Autowired
    private AccountService accountService;

    private Map<Integer, Question> questionMap = Maps.newConcurrentMap();

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String picUrlPrefix = ConfigUtils.domainName()+"/images/";
    private String audioUrlPrefix = ConfigUtils.domainName()+"/audio/";

    private final static String shortUrlService = "http://tinyurl.com/api-create.php?url=";

    private static final Integer PREPARED_WEEK = 0;

    @PostConstruct
    public void initQuestion(){
        List<Question> questionList = questionDao.loadAll(Question.class);
        List<Choice> choiceList = choiceDao.loadAll(Choice.class);
        for(Question question:questionList){
            List<Choice> choices = Lists.newArrayList();
            for(Choice choice:choiceList){
                if(choice.getQuestionId().equals(question.getId())){
                    choices.add(choice);
                }
            }
            question.setChoiceList(choices);
            questionMap.put(question.getId(), question);
            //语音分析，拼接完整url
            if(question.getAnalysis()!=null && question.getAnalysisType()==3){
                question.setAnalysis(audioUrlPrefix+question.getAnalysis());
            }
        }

    }

    public Page loadPage(String openid, Integer chapterId, Integer pageSequence, Boolean lazyLoad) {
        Assert.notNull(openid, "openid不能为空");
        //首次学习
        if(pageSequence==null){
            pageSequence = 1;
        }

        Page page = pageDao.loadPage(chapterId, pageSequence);
        if(page!=null) {
            List<Material> materialList = materialDao.loadPageMaterials(page.getId());
            //拼接url前缀
            for(Material m:materialList){
                materialHandle(openid, chapterId, m);
            }
            page.setMaterialList(materialList);
            //记录到阅读到第几页
            if(!lazyLoad) {
                markPage(openid, chapterId, pageSequence);
            }
        }
        return page;
    }

    private void materialHandle(String openid, Integer chapterId, Material m) {
        //图片，语音加前缀
        if(m.getType()==2){
            m.setContent(picUrlPrefix+m.getContent());
        }else if(m.getType()==3){
            m.setContent(audioUrlPrefix+m.getContent());
        //占位符替换，当文字处理
        }else if(m.getType()==11){
            m.setContent(classPlaceholder(m.getContent(), chapterId, openid));
            m.setType(1);
        }else if(m.getType()==12){
            m.setContent(classMemberPlaceholder(m.getContent(), chapterId, openid));
            m.setType(1);
        }else if(m.getType()==13){
            m.setContent(accountPlaceholder(m.getContent(), openid));
            m.setType(1);
        //占位符替换，当图片处理
        }else if(m.getType()==21){
            m.setContent(picUrlPrefix+classPlaceholder(m.getContent(), chapterId, openid));
            m.setType(2);
        }
    }

    private String accountPlaceholder(String content, String openid) {
        Assert.notNull(openid, "openid不能为空");
        Account account = accountService.getAccount(openid, false);
        if(account==null){
            logger.error("openid {} is invalid", openid);
            return content;
        }
        //去除“[表情]”文字
        account.setNickname(account.getNickname().replaceAll("[表情]", ""));

        String json = new Gson().toJson(account);
        Map<String, String> memos = new Gson().fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());

        return CommonUtils.placeholderReplace(content, memos);
    }

    private String classMemberPlaceholder(String content, Integer chapterId, String openid) {
        Assert.notNull(openid, "openid不能为空");
        Chapter chapter = chapterDao.load(Chapter.class, chapterId);
        if(chapter==null){
            logger.error("chapterId {} is invalid", chapterId);
            return content;
        }
        ClassMember classMember = classMemberDao.activeCourse(openid, chapter.getCourseId());
        if(classMember==null){
            //未报名不能获取数据
            logger.error("{} has no active course", openid);
            return content;
        }

        String json = new Gson().toJson(classMember);
        Map<String, String> memos = new Gson().fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());

        return CommonUtils.placeholderReplace(content, memos);
    }

    private String classPlaceholder(String content, Integer chapterId, String openid) {
        Assert.notNull(openid, "openid不能为空");
        Chapter chapter = chapterDao.load(Chapter.class, chapterId);
        if(chapter==null){
            logger.error("chapterId {} is invalid", chapterId);
            return content;
        }
        ClassMember classMember = classMemberDao.activeCourse(openid, chapter.getCourseId());
        if(classMember==null){
            //未报名不能获取数据
            logger.error("{} has no active course", openid);
            return content;
        }
        QuanwaiClass quanwaiClass = classDao.load(QuanwaiClass.class, classMember.getClassId());
        if(quanwaiClass==null){
            //未报名不能获取数据
            logger.error("classId {} is invalid", classMember.getClassId());
            return content;
        }

        String json = new Gson().toJson(quanwaiClass);
        Map<String, String> memos = new Gson().fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());

        return CommonUtils.placeholderReplace(content, memos);
    }

    public Chapter loadChapter(String openid, Integer chapterId) {
        Assert.notNull(openid, "openid不能为空");
        Chapter chapter = chapterDao.load(Chapter.class, chapterId);
        if(chapter==null){
            logger.error("chapterId {} is invalid", chapterId);
            return null;
        }

        Integer totalPage = pageDao.chapterPageNumber(chapterId);
        chapter.setTotalPage(totalPage);

        return chapter;
    }

    public Question loadQuestion(String openid, Integer questionId) {
        Assert.notNull(openid, "openid不能为空");
        Question question = questionMap.get(questionId);
        if(question==null){
            logger.error("questionId {} is invalid", questionId);
            return null;
        }
        ClassMember classMember = classMemberDao.activeCourse(openid, question.getCourseId());
        if(classMember==null){
            //未报名不能获取数据
            logger.error("{} has no active course", openid);
            return null;
        }

        boolean submitted = questionSubmitDao.submitted(openid, classMember.getClassId(), questionId);
        question.setAnswered(submitted);

        return question;
    }

    public Homework loadHomework(String openid, Integer homeworkId) {
        Assert.notNull(openid, "openid不能为空");
        Homework homework = homeworkDao.load(Homework.class, homeworkId);
        if(homework==null){
            logger.error("homeworkId {} is invalid", homeworkId);
            return null;
        }
        ClassMember classMember = classMemberDao.activeCourse(openid, homework.getCourseId());
        if(classMember==null){
            //未报名不能获取数据
            logger.error("{} has no active course", openid);
            return null;
        }

        HomeworkSubmit submit = homeworkSubmitDao.loadHomeworkSubmit(openid, classMember.getClassId(), homeworkId);
        if(submit==null || submit.getSubmitContent()==null) {
            homework.setSubmitted(false);
        }else{
            homework.setSubmitted(true);
        }
        if(homework.getVoice()!=null) {
            homework.setVoice(ConfigUtils.domainName() + homework.getVoice());
        }
        if(submit==null){
            String url = "/static/h?id="+ CommonUtils.randomString(6);
            String shortUrl = generateShortUrl(ConfigUtils.domainName()+url);
            homework.setPcurl(shortUrl);
            homeworkSubmitDao.insert(openid, classMember.getClassId(), homeworkId, url, shortUrl);
        }else{
            if(submit.getSubmitUrl()!=null){
                homework.setPcurl(submit.getShortUrl());
            }
            homework.setContent(submit.getSubmitContent());
        }

        return homework;
    }

    private String generateShortUrl(String url) {
        String requestUrl = shortUrlService;
        try {
            requestUrl = requestUrl + URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException ignored) {

        }
        return restfulHelper.getPlain(requestUrl);
    }

    public HomeworkSubmit loadHomework(String url) {
        return homeworkSubmitDao.loadByUrl(url);
    }

    public List<HomeworkSubmit> loadSubmittedHomework(Integer homeworkId) {
        return homeworkSubmitDao.submittedHomework(homeworkId);
    }

    public void submitHomework(String content, String openid, Integer homeworkId) {
        Assert.notNull(openid, "openid不能为空");
        Homework homework = homeworkDao.load(Homework.class, homeworkId);
        if(homework==null){
            logger.error("homeworkId {} is invalid", homeworkId);
            return;
        }
        ClassMember classMember = classMemberDao.activeCourse(openid, homework.getCourseId());
        if(classMember==null){
            //未报名不能获取数据
            logger.error("{} has no active course", openid);
            return;
        }

        homeworkSubmitDao.submit(homeworkId, classMember.getClassId(), openid, content);
        completeHomework(homeworkId, classMember);
    }

    //完成作业章节
    private void completeHomework(Integer homeworkId, ClassMember classMember) {
        Assert.notNull(classMember, "classMember不能为空");
        QuanwaiClass quanwaiClass = classDao.load(QuanwaiClass.class, classMember.getClassId());
        List<Chapter> chapters = chapterDao.loadChapters(quanwaiClass.getCourseId());
        List<Chapter> homework = Lists.newArrayList();
        for(Chapter chapter:chapters){
            if(chapter.getType()== CourseType.HOMEWORK){
                homework.add(chapter);
            }
        }
        List<Integer> chapterIds = Lists.newArrayList();
        for(Chapter chapter:homework){
            chapterIds.add(chapter.getId());
        }

        Map<Integer, Integer> pageChapterMap = Maps.newHashMap();
        List<Page> pages = pageDao.loadPages(chapterIds);
        List<Integer> pageIds = Lists.newArrayList();
        for(Page page:pages){
            pageIds.add(page.getId());
            pageChapterMap.put(page.getId(), page.getChapterId());
        }

        List<Material> materials = materialDao.loadPageMaterials(pageIds);
        for(Material material:materials){
            if(homeworkId.toString().equals(material.getContent())){
                Chapter chapter = chapterDao.load(Chapter.class, pageChapterMap.get(material.getPageId()));
                completeChapter(classMember.getOpenId(), chapter);
            }
        }
    }

    public boolean submitQuestion(String openid, Integer questionId, List<Integer> choiceList) {
        Assert.notNull(openid, "openid不能为空");
        String answer = "";
        Question q = questionMap.get(questionId);
        if(q==null){
            logger.error("questionId {} is invalid", questionId);
            return false;
        }
        ClassMember classMember = classMemberDao.activeCourse(openid,q.getCourseId());
        if(classMember==null){
            //未报名不能获取数据
            logger.error("{} has no active course", openid);
            return false;
        }
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
        Chapter chapter = chapterDao.load(Chapter.class, chapterId);
        if(chapter==null){
            logger.error("chapterId {} is invalid", chapterId);
            return;
        }
        if(chapter.getType()==CourseType.HOMEWORK){
            return;
        }
        completeChapter(openid, chapter);
    }

    public void completeChapter(String openid, Chapter chapter) {
        ClassMember classMember = classMemberDao.activeCourse(openid, chapter.getCourseId());
        if(classMember==null){
            //未报名不能获取数据
            logger.error("{} has no active course", openid);
            return;
        }
        String progress = progressMark(classMember.getComplete(), chapter.getSequence());
        if(progress!=null) {
            classMemberDao.complete(openid, classMember.getClassId(), progress);
        }
    }

    public void remark(String openid, Integer classId, Integer homeworkId, boolean excellent, boolean fail) {
        int score = getScore(excellent, fail);
        homeworkSubmitDao.remark(homeworkId, classId, openid, null, score);
    }

    public void markPage(String openid, Integer chapterId, Integer pageSequence) {
        currentChapterPageDao.updatePage(openid, chapterId, pageSequence);
        //判断第一页
        if(pageSequence==1){
            progressChapter(openid, chapterId);
        }

        //判断是否最后一页
        int count = pageDao.chapterPageNumber(chapterId);
        if(count==pageSequence){
            completeChapter(openid, chapterId);
        }
    }

    private void progressChapter(String openid, Integer chapterId) {
        Chapter chapter = chapterDao.load(Chapter.class, chapterId);
        if(chapter==null){
            logger.error("chapterId {} is invalid", chapterId);
            return;
        }
        ClassMember classMember = classMemberDao.activeCourse(openid, chapter.getCourseId());
        if(classMember==null){
            //未报名不能获取数据
            logger.error("{} has no active course", openid);
            return;
        }
        String progress = progressMark(classMember.getProgress(), chapter.getSequence());
        if(progress!=null) {
            classMemberDao.progress(openid, classMember.getClassId(), progress);
        }
    }

    private String progressMark(String progress, Integer sequence){
        boolean mark = false;
        if(StringUtils.isEmpty(progress)){
            progress = sequence +"";
        }else{
            String[] progressArr = progress.split(",");
            for(String prog:progressArr){
                if(prog.equals(String.valueOf(sequence))){
                    mark = true;
                }
            }
            if(!mark) {
                progress = progress+","+sequence;
            }
        }
        if(!mark){
            return progress;
        }else{
            return null;
        }
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

        List<Choice> all = question.getChoiceList();
        List<Choice> right = Lists.newArrayList();
        for(Choice choice:all){
            if(choice.getRight()){
                right.add(choice);
            }
        }

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

    public CourseWeek loadCourseWeek(Integer courseId, Integer week) {
        return courseWeekDao.getCourseWeek(courseId, week);
    }

    public Chapter loadFirstPreparedChapter(Integer courseId) {
        List<Chapter> chapters = chapterDao.loadChapters(courseId, PREPARED_WEEK);
        //初始化序号
        int first = 0;
        Chapter firstChapter = null;

        for(Chapter chapter:chapters){
            if(chapter.getSequence()<first){
                first = chapter.getSequence();
                firstChapter = chapter;
            }
        }
        return firstChapter;
    }

    public void reloadQuestion() {
        initQuestion();
    }
}
