package com.iquanwai.confucius.biz.domain.course.progress;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.course.*;
import com.iquanwai.confucius.biz.po.*;
import com.iquanwai.confucius.biz.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

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

    private Logger logger = LoggerFactory.getLogger(getClass());


    public ClassMember loadActiveCourse(String openid, Integer courseId) {
        Assert.notNull(openid, "openid不能为空");
        ClassMember classMember = classMemberDao.activeCourse(openid);
        if(courseId!=null && courseId.equals(classMember.getCourseId())){
            return null;
        }
        if(classMember==null){
            return null;
        }
        //设置课程id和课程进度
        QuanwaiClass quanwaiClass = classDao.load(QuanwaiClass.class, classMember.getClassId());
        if(quanwaiClass!=null){
            classMember.setCourseId(quanwaiClass.getCourseId());
            classMember.setClassProgress(quanwaiClass.getProgress());
        }
        return classMember;
    }

    public Course loadCourse(int courseId, int week, List<Integer> personalProgress, int classProgress) {
        Course course = courseDao.load(Course.class, courseId);
        List<Chapter> chapters = chapterDao.loadChapters(courseId, week);

        course.setChapterList(buildChapter(chapters, personalProgress, classProgress));

        return course;
    }

    public void classProgress() {
        List<QuanwaiClass> openClass = classDao.loadAllOpenClass();
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
                    chapter.setPageSequence(currentChapterPage.getPageSequence());
                }
            }
        }

    }

    private List<Chapter> buildChapter(List<Chapter> chapters, final List<Integer> personalProgress, final int classProgress) {
        return Lists.transform(chapters, new Function<Chapter, Chapter>() {
            public Chapter apply(Chapter chapter) {
                boolean unlock = checkUnlock(chapter, classProgress);
                boolean complete = checkComplete(chapter, personalProgress);
                chapter.setIcon(CourseType.getUrl(chapter.getType(), unlock, complete));
                chapter.setUnlock(unlock);
                chapter.setComplete(complete);
                return chapter;
            }
        });
    }

    private boolean checkComplete(Chapter chapter, List<Integer> personalProgress) {
        Assert.notNull(chapter, "chapter不能为空");
        Assert.notNull(personalProgress, "个人进度不能为空");
        if(personalProgress.contains(chapter.getId())){
            return true;
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
                chapter.getType()==CourseType.ASSESSMENT){
            return true;
        }

        return false;
    }
}
