package com.iquanwai.confucius.biz.domain.course.progress;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.course.*;
import com.iquanwai.confucius.biz.domain.course.signup.ClassMemberCountRepo;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.confucius.biz.po.*;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by justin on 16/8/29.
 */
@Service
public class CourseProgressServiceImpl implements CourseProgressService {
    @Autowired
    private CourseDao courseDao;
    @Autowired
    private ChapterDao chapterDao;
    @Autowired
    private ClassDao classDao;
    @Autowired
    private ClassMemberDao classMemberDao;
    @Autowired
    private CurrentChapterPageDao currentChapterPageDao;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private ClassMemberCountRepo classMemberCountRepo;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final static String CERTIFICATE_PREFIX = "IQW";

    private final static int CERTIFICATE_OFFSET = 51000;


    public ClassMember loadActiveCourse(String openid, Integer courseId) {
        Assert.notNull(openid, "openid不能为空");
        ClassMember classMember = classMemberDao.activeCourse(openid);

        if(classMember==null){
            logger.error("{} has no active course", openid);
            return null;
        }
        //设置课程id和课程进度
        QuanwaiClass quanwaiClass = classDao.load(QuanwaiClass.class, classMember.getClassId());
        if(quanwaiClass!=null){
            classMember.setClassProgress(quanwaiClass.getProgress());
        }
        return classMember;
    }

    public Course loadCourse(ClassMember classMember, int week) {
        Course course = courseDao.load(Course.class, classMember.getCourseId());
        List<Chapter> chapters = chapterDao.loadChapters(classMember.getCourseId(), week);

        course.setChapterList(buildChapter(chapters, classMember.getComplete(), classMember.getClassProgress()));

        return course;
    }

    public void classProgress() {
        List<QuanwaiClass> openClass = classDao.loadRunningClass();
        for(QuanwaiClass clazz:openClass){
            Integer courseId = clazz.getCourseId();
            //开课天数=今天-开课日期+1
            int startDay = DateUtils.interval(clazz.getOpenTime())+1;
            Chapter chapter = chapterDao.getChapterByStartDay(courseId, startDay);
            if(chapter!=null){
                Integer sequence = chapter.getSequence();
                if(sequence==null){
                    logger.error("{} has no sequence", chapter.getId());
                }else {
                    if (!sequence.equals(clazz.getProgress())) {
                        classDao.progress(clazz.getId(), sequence);
                    }
                }
            }
        }
        classMemberCountRepo.initClass();
    }

    public void personalChapterPage(String openid, List<Chapter> chapters) {
        List<Integer> chapterIds = Lists.transform(chapters, new Function<Chapter, Integer>() {
            public Integer apply(Chapter input) {
                return input.getId();
            }
        });

        //设置学员的章节上次看到的页码
        List<CurrentChapterPage> currentChapterPages = currentChapterPageDao.currentPages(openid, chapterIds);
        for(Chapter chapter:chapters){
            for(CurrentChapterPage currentChapterPage:currentChapterPages){
                if(chapter.getId()==currentChapterPage.getChapterId()){
                    //如果用户已经学习完，则从第一页开始学习
                    if(!chapter.isComplete()) {
                        chapter.setPageSequence(currentChapterPage.getPageSequence());
                    }else{
                        chapter.setPageSequence(1);
                    }
                }
            }
        }

    }

    public void graduate(Integer classId) {
        List<ClassMember> classMembers = classMemberDao.getPassMember(classId);
        String key = ConfigUtils.coursePassMsgKey();
        for(ClassMember classMember:classMembers){
            boolean pass = classMember.getPass();
            boolean superb = classMember.getSuperb();

            TemplateMessage templateMessage = new TemplateMessage();
            templateMessage.setTouser(classMember.getOpenId());

            templateMessage.setTemplate_id(key);
            Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
            templateMessage.setData(data);
            //生成毕业证书
            String certificateNo = generateCertificate(classMember);
            classMemberDao.updateCertificateNo(classId, classMember.getOpenId(), certificateNo);

            Course course = courseDao.load(Course.class, classMember.getCourseId());

            String first = coursePassStartMsg(pass, superb, course.getName());
            String remark = courseRemarkStartMsg(pass, superb);
            data.put("first",new TemplateMessage.Keyword(first));
            data.put("keyword1",new TemplateMessage.Keyword(course.getName()));
            data.put("keyword2",new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
            data.put("remark",new TemplateMessage.Keyword(remark));
            //TODO:url待定
//            templateMessage.setUrl(quanwaiClass.getWeixinGroup());
            templateMessageService.sendMessage(templateMessage);
        }

        classMemberDao.graduate(classId);
    }

    public void closeClassEntry() {
        Date date = DateUtils.afterDays(new Date(), 1);
        List<QuanwaiClass> openClasses = classDao.loadClassByOpenDate(date);
        for(QuanwaiClass quanwaiClass:openClasses){
            classDao.closeEntry(quanwaiClass.getId());
        }
    }

    private String generateCertificate(ClassMember classMember) {

        return String.format("%s%02d%02d%08d%s", CERTIFICATE_PREFIX,
                classMember.getCourseId(),
                new Random().nextInt(100),
                classMember.getId()+CERTIFICATE_OFFSET,
                classMember.getMemberId());
    }

    private String courseRemarkStartMsg(boolean pass, boolean superb) {
        if(pass){
            if(superb){
                return "作为优秀学员，你的奖励是待定\n" +
                        "现在获取你的专属毕业证书吧！";
            }else{
                return "现在获取你的专属毕业证书吧！";
            }
        }else{
            return "在下周之内补完作业，还可以顺利毕业、拿到专属毕业证书哦！";
        }
    }

    private String coursePassStartMsg(boolean pass, boolean superb, String courseName) {
        if(pass){
            if(superb){
                return "你已完成"+courseName+"训练营的所有挑战，并且以出色的成绩，作为“优秀学员”毕业，给自己一个拥抱吧。";
            }else{
                return "你已完成"+courseName+"训练营的所有挑战，不知不觉就走完了这段学习历程。";
            }
        }else{
            return "很遗憾你未能完成"+courseName+"训练营的所有挑战";
        }
    }

    private List<Chapter> buildChapter(List<Chapter> chapters, final String personalCompleteProgress, final int classProgress) {
        return Lists.transform(chapters, new Function<Chapter, Chapter>() {
            public Chapter apply(Chapter chapter) {
                boolean unlock = checkUnlock(chapter, classProgress);
                boolean complete = checkComplete(chapter, personalCompleteProgress);
                chapter.setIcon(CourseType.getUrl(chapter.getType(), unlock, complete));
                chapter.setUnlock(unlock);
                chapter.setComplete(complete);
                return chapter;
            }
        });
    }

    private boolean checkComplete(Chapter chapter, String personalProgress) {
        Assert.notNull(chapter, "chapter不能为空");
        if(personalProgress==null){
            return false;
        }

        String[] arr = personalProgress.split(",");
        for(String completeChapter:arr){
            try {
                if (Integer.valueOf(completeChapter).equals(chapter.getSequence())) {
                    return true;
                }
            }catch (NumberFormatException e){
                logger.error("{} is invalid", personalProgress);
            }
        }

        return false;
    }

    private boolean checkUnlock(Chapter chapter, int classProgress) {
        Assert.notNull(chapter, "chapter不能为空");
        //章节进度小于课程当前进度，则当前章节解锁
        if(chapter.getStartDay()<=classProgress){
            return true;
        }

        if(chapter.getType()==CourseType.RELAX||
                chapter.getType()==CourseType.ASSESSMENT||
                chapter.getType()==CourseType.GRADUATE){
            if(chapter.getStartDay()==classProgress && chapter.getEndDay()==classProgress) {
                return true;
            }
        }

        return false;
    }
}
