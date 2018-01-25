package com.iquanwai.confucius.biz.domain.course.progress;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.iquanwai.confucius.biz.dao.course.*;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.Material;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.systematism.*;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private RestfulHelper restfulHelper;
    @Autowired
    private ClassDao classDao;
    @Autowired
    private AccountService accountService;

    private Pattern pattern = Pattern.compile("\\{\\d+\\}");

    private Map<Integer, Question> questionMap = Maps.newConcurrentMap();

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final static String shortUrlService = "http://tinyurl.com/api-create.php?url=";

    @PostConstruct
    public void initQuestion() {
        List<Question> questionList = questionDao.loadAll(Question.class);
        List<Choice> choiceList = choiceDao.loadAll(Choice.class);
        for (Question question : questionList) {
            List<Choice> choices = Lists.newArrayList();
            choices.addAll(choiceList.stream().filter(choice -> choice.getQuestionId().
                    equals(question.getId())).collect(Collectors.toList()));
            question.setChoiceList(choices);
            questionMap.put(question.getId(), question);
            //语音分析，拼接完整url
            if (question.getVoice() != null) {
                question.setVoice(ConfigUtils.resourceDomainName() + "/audio/" + question.getVoice());
            }
        }

    }

    @Override
    public Page loadPage(Integer profileId, Integer chapterId, Integer pageSequence, Boolean lazyLoad) {
        //首次学习
        if (pageSequence == null) {
            pageSequence = 1;
        }

        Page page = pageDao.loadPage(chapterId, pageSequence);
        if (page != null) {
            List<Material> materialList = materialDao.loadPageMaterials(page.getId());
            //拼接url前缀
            for (Material m : materialList) {
                materialHandle(profileId, chapterId, m);
            }
            page.setMaterialList(materialList);
            //记录到阅读到第几页
            if (!lazyLoad) {
                markPage(profileId, chapterId, pageSequence);
            }
        }
        return page;
    }

    private void materialHandle(Integer profileId, Integer chapterId, Material m) {
        Assert.notNull(m, "material不能为空");
        //图片，语音加前缀
        if (m.getType() == 2) {
            m.setContent(ConfigUtils.resourceDomainName() + "/images/" + m.getContent());
        } else if (m.getType() == 3) {
            m.setContent(ConfigUtils.resourceDomainName() + "/audio/" + m.getContent());
            //占位符替换，当文字处理
        } else if (m.getType() == 11) {
            m.setContent(classPlaceholder(m.getContent(), chapterId, profileId));
            m.setType(1);
        } else if (m.getType() == 12) {
            m.setContent(classMemberPlaceholder(m.getContent(), chapterId, profileId));
            m.setType(1);
        } else if (m.getType() == 13) {
            m.setContent(accountPlaceholder(m.getContent(), profileId));
            m.setType(1);
            //占位符替换，当图片处理
        } else if (m.getType() == 21) {
            m.setContent(classPlaceholder(m.getContent(), chapterId, profileId));
            m.setType(2);
        } else if (m.getType() == 31) {
            // 支付链接，占位符替换，当文字处理
            Matcher matcher = pattern.matcher(m.getContent());
            String courseId = null;
            String placeholder = null;
            if (matcher.find()) {
                placeholder = matcher.group();
                courseId = placeholder.substring(1, placeholder.length() - 1);
            }

            if (courseId == null) {
                logger.error("查询该章节对应的正式课程失败,素材id:{}", m.getId());
            } else {
                m.setContent(m.getContent().replace(placeholder,
                        ConfigUtils.domainName() + "/pay/signup?courseId=" + courseId));
                m.setType(1);
            }
        }
    }

    private String accountPlaceholder(String content, Integer profileId) {
        Profile account = accountService.getProfile(profileId);
        if (account == null) {
            logger.error("profileId {} is invalid", profileId);
            return content;
        }

        String json = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create().toJson(account);
        Map<String, String> memos = new Gson().fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());

        return CommonUtils.placeholderReplace(content, memos);
    }

    private String classMemberPlaceholder(String content, Integer chapterId, Integer profileId) {
        Chapter chapter = chapterDao.load(Chapter.class, chapterId);
        if (chapter == null) {
            logger.error("chapterId {} is invalid", chapterId);
            return content;
        }
        ClassMember classMember = classMemberDao.classMember(profileId, chapter.getCourseId());
        if (classMember == null) {
            //未报名不能获取数据
            logger.error("{} has no active course", profileId);
            return content;
        }

        String json = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create().toJson(classMember);
        Map<String, String> memos = new Gson().fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());

        return CommonUtils.placeholderReplace(content, memos);
    }

    private String classPlaceholder(String content, Integer chapterId, Integer profileId) {
        Chapter chapter = chapterDao.load(Chapter.class, chapterId);
        if (chapter == null) {
            logger.error("chapterId {} is invalid", chapterId);
            return content;
        }
        ClassMember classMember = classMemberDao.classMember(profileId, chapter.getCourseId());
        if (classMember == null) {
            //未报名不能获取数据
            logger.error("{} has no active course", profileId);
            return content;
        }
        QuanwaiClass quanwaiClass = classDao.load(QuanwaiClass.class, classMember.getClassId());
        if (quanwaiClass == null) {
            //未报名不能获取数据
            logger.error("classId {} is invalid", classMember.getClassId());
            return content;
        }

        String json = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create().toJson(quanwaiClass);
        Map<String, String> memos = new Gson().fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());

        return CommonUtils.placeholderReplace(content, memos);
    }

    @Override
    public Chapter loadChapter(Integer chapterId) {
        Chapter chapter = chapterDao.load(Chapter.class, chapterId);
        if (chapter == null) {
            logger.error("chapterId {} is invalid", chapterId);
            return null;
        }

        Integer totalPage = pageDao.chapterPageNumber(chapterId);
        chapter.setTotalPage(totalPage);

        return chapter;
    }

    @Override
    public Question loadQuestion(Integer profileId, Integer questionId) {
        Question question = questionMap.get(questionId);
        if (question == null) {
            logger.error("questionId {} is invalid", questionId);
            return null;
        }
        ClassMember classMember = classMemberDao.classMember(profileId, question.getCourseId());
        if (classMember == null) {
            //未报名不能获取数据
            logger.error("{} has no active course", profileId);
            return null;
        }

        boolean submitted = questionSubmitDao.submitted(profileId, classMember.getClassId(), questionId);
        question.setAnswered(submitted);

        return question;
    }

    @Override
    public Homework loadHomework(Integer profileId, Integer homeworkId) {
        Homework homework = homeworkDao.load(Homework.class, homeworkId);
        if (homework == null) {
            logger.error("homeworkId {} is invalid", homeworkId);
            return null;
        }
        ClassMember classMember = classMemberDao.classMember(profileId, homework.getCourseId());
        if (classMember == null) {
            //未报名不能获取数据
            logger.error("{} has no active course", profileId);
            return null;
        }

        HomeworkSubmit submit = homeworkSubmitDao.loadHomeworkSubmit(profileId, classMember.getClassId(), homeworkId);
        if (submit == null || submit.getSubmitContent() == null) {
            homework.setSubmitted(false);
        } else {
            homework.setSubmitted(true);
        }
        if (homework.getVoice() != null) {
            homework.setVoice(ConfigUtils.resourceDomainName() + homework.getVoice());
        }
        if (submit == null) {
            String url = "/static/h?id=" + CommonUtils.randomString(6);
            String shortUrl = generateShortUrl(ConfigUtils.domainName() + url);
            homework.setPcurl(shortUrl);
            HomeworkSubmit homeworkSubmit = new HomeworkSubmit();
            homeworkSubmit.setSubmitProfileId(profileId);
            homeworkSubmit.setClassId(classMember.getClassId());
            homeworkSubmit.setHomeworkId(homeworkId);
            homeworkSubmit.setSubmitUrl(url);
            if (!shortUrl.equals(ConfigUtils.domainName() + url)) {
                homeworkSubmit.setShortUrl(shortUrl);
            }
            homeworkSubmitDao.insert(homeworkSubmit);
        } else {
            if (submit.getSubmitUrl() != null) {
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
        //FIX:偶尔调用失败的bug
        String shortUrl = "";
        ResponseBody responseBody = restfulHelper.getPlain(requestUrl);
        if (responseBody != null) {
            try {
                shortUrl = responseBody.string();
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        if (shortUrl.startsWith("http")) {
            return shortUrl;
        } else {
            return url;
        }
    }

    @Override
    public HomeworkSubmit loadHomework(String url) {
        return homeworkSubmitDao.loadByUrl(url);
    }

    @Override
    public List<HomeworkSubmit> loadSubmittedHomework(Integer homeworkId) {
        return homeworkSubmitDao.submittedHomework(homeworkId);
    }

    @Override
    public void submitHomework(String content, Integer profileId, Integer homeworkId) {
        Homework homework = homeworkDao.load(Homework.class, homeworkId);
        if (homework == null) {
            logger.error("homeworkId {} is invalid", homeworkId);
            return;
        }
        ClassMember classMember = classMemberDao.classMember(profileId, homework.getCourseId());
        if (classMember == null) {
            //未报名不能获取数据
            logger.error("{} has no active course", profileId);
            return;
        }

        homeworkSubmitDao.submit(homeworkId, classMember.getClassId(), profileId, content);
        completeHomework(homeworkId, classMember);
    }

    //完成作业章节
    private void completeHomework(Integer homeworkId, ClassMember classMember) {
        Assert.notNull(classMember, "classMember不能为空");
        QuanwaiClass quanwaiClass = classDao.load(QuanwaiClass.class, classMember.getClassId());
        List<Chapter> chapters = chapterDao.loadChapters(quanwaiClass.getCourseId());
        List<Chapter> homework = Lists.newArrayList();

        homework.addAll(chapters.stream().filter(chapter -> chapter.getType() == CourseType.HOMEWORK
                || chapter.getType() == CourseType.NEW_HOMEWORK).collect(Collectors.toList()));

        List<Integer> chapterIds = Lists.newArrayList();
        chapterIds.addAll(homework.stream().map(Chapter::getId).collect(Collectors.toList()));

        Map<Integer, Integer> pageChapterMap = Maps.newHashMap();
        List<Page> pages = pageDao.loadPages(chapterIds);
        List<Integer> pageIds = Lists.newArrayList();
        for (Page page : pages) {
            pageIds.add(page.getId());
            pageChapterMap.put(page.getId(), page.getChapterId());
        }

        List<Material> materials = materialDao.loadPageMaterials(pageIds);

        materials.stream().filter(material -> homeworkId.toString().equals(material.getContent())).forEach(material -> {
            Chapter chapter = chapterDao.load(Chapter.class, pageChapterMap.get(material.getPageId()));
            completeChapter(classMember.getProfileId(), chapter);
        });
    }

    @Override
    public boolean submitQuestion(Integer profileId, Integer questionId, List<Integer> choiceList) {
        String answer = "";
        Question q = questionMap.get(questionId);
        if (q == null) {
            logger.error("questionId {} is invalid", questionId);
            return false;
        }
        ClassMember classMember = classMemberDao.classMember(profileId, q.getCourseId());
        if (classMember == null) {
            //未报名不能获取数据
            logger.error("{} has no active course", profileId);
            return false;
        }
        Integer score = score(q, choiceList);
        boolean right = false;
        if (score.equals(q.getPoint())) {
            right = true;
        }
        for (Integer choice : choiceList) {
            answer = answer + "," + choice;
        }

        if (StringUtils.isNotEmpty(answer)) {
            answer = answer.substring(1);
            QuestionSubmit questionSubmit = new QuestionSubmit();
            questionSubmit.setClassId(classMember.getClassId());
            questionSubmit.setScore(score);
            questionSubmit.setQuestionId(questionId);
            questionSubmit.setSubmitAnswer(answer);
            questionSubmit.setSubmitProfileId(profileId);
            questionSubmit.setIsRight(right ? 1 : 0);
            questionSubmitDao.insert(questionSubmit);
        }
        return right;
    }

    @Override
    public void completeChapter(Integer profileId, Integer chapterId) {
        Chapter chapter = chapterDao.load(Chapter.class, chapterId);
        if (chapter == null) {
            logger.error("chapterId {} is invalid", chapterId);
            return;
        }
        if (chapter.getType() == CourseType.HOMEWORK || chapter.getType() == CourseType.NEW_HOMEWORK) {
            return;
        }
        completeChapter(profileId, chapter);
    }

    private void completeChapter(Integer profileId, Chapter chapter) {
        Assert.notNull(chapter, "chapter不能为空");
        ClassMember classMember = classMemberDao.classMember(profileId, chapter.getCourseId());
        if (classMember == null) {
            //未报名不能获取数据
            logger.error("{} has no active course", profileId);
            return;
        }
        String progress = progressMark(classMember.getComplete(), chapter.getSequence());
        if (progress != null) {
            classMemberDao.complete(profileId, classMember.getClassId(), progress);
        }
    }

    @Override
    public void remark(Integer profileId, Integer classId, Integer homeworkId, boolean excellent, boolean fail) {
        int score = getScore(excellent, fail);
        homeworkSubmitDao.remark(homeworkId, classId, profileId, null, score);
    }

    @Override
    public void markPage(Integer profileId, Integer chapterId, Integer pageSequence) {
        currentChapterPageDao.updatePage(profileId, chapterId, pageSequence);
        //判断第一页
        if (pageSequence == 1) {
            progressChapter(profileId, chapterId);
        }

        //判断是否最后一页
        int count = pageDao.chapterPageNumber(chapterId);
        if (count == pageSequence) {
            completeChapter(profileId, chapterId);
        }
    }

    private void progressChapter(Integer profileId, Integer chapterId) {
        Chapter chapter = chapterDao.load(Chapter.class, chapterId);
        if (chapter == null) {
            logger.error("chapterId {} is invalid", chapterId);
            return;
        }
        ClassMember classMember = classMemberDao.classMember(profileId, chapter.getCourseId());
        if (classMember == null) {
            //未报名不能获取数据
            logger.error("{} has no active course", profileId);
            return;
        }
        String progress = progressMark(classMember.getProgress(), chapter.getSequence());
        if (progress != null) {
            classMemberDao.progress(profileId, classMember.getClassId(), progress);
        }
    }

    private String progressMark(String progress, Integer sequence) {
        boolean mark = false;
        if (StringUtils.isEmpty(progress)) {
            progress = sequence + "";
        } else {
            String[] progressArr = progress.split(",");
            for (String prog : progressArr) {
                if (prog.equals(String.valueOf(sequence))) {
                    mark = true;
                }
            }
            if (!mark) {
                progress = progress + "," + sequence;
            }
        }
        if (!mark) {
            return progress;
        } else {
            return null;
        }
    }

    private int getScore(boolean excellent, boolean fail) {
        int score = 75;
        if (excellent) {
            score = 90;
        }
        if (fail) {
            score = 59;
        }
        return score;
    }

    private Integer score(Question question, List<Integer> choiceList) {
        Assert.notNull(choiceList, "选项不能为空");

        List<Choice> all = question.getChoiceList();
        List<Choice> right = Lists.newArrayList();
        right.addAll(all.stream().filter(Choice::getRight).
                collect(Collectors.toList()));

        for (Choice choice : right) {
            if (!choiceList.contains(choice.getId())) {
                return 0;
            }
        }

        if (right.size() == choiceList.size()) {
            return question.getPoint();
        }
        return 0;
    }

}
