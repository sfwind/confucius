package com.iquanwai.confucius.biz.domain.course.progress;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.ChapterDao;
import com.iquanwai.confucius.biz.dao.ClassDao;
import com.iquanwai.confucius.biz.dao.ClassMemberDao;
import com.iquanwai.confucius.biz.dao.CourseDao;
import com.iquanwai.confucius.biz.dao.po.Chapter;
import com.iquanwai.confucius.biz.dao.po.ClassMember;
import com.iquanwai.confucius.biz.dao.po.Course;
import com.iquanwai.confucius.biz.dao.po.QuanwaiClass;
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

    public Course loadCourse(int courseId, int week, int personalProgress, int classProgress) {
        Course course = courseDao.load(Course.class, courseId);
        List<Chapter> chapters = chapterDao.loadChapters(courseId, week);

        course.setChapterList(buildChapter(chapters, classProgress));

        return course;
    }

    private List<Chapter> buildChapter(List<Chapter> chapters, final int classProgress) {
        return Lists.transform(chapters, new Function<Chapter, Chapter>() {
            public Chapter apply(Chapter chapter) {
                boolean unlock = checkUnlock(chapter, classProgress);
                boolean complete = checkComplete(chapter, classProgress);
                chapter.setIcon(CourseType.getUrl(chapter.getType(), unlock, complete));
                chapter.setUnlock(unlock);
                chapter.setComplete(complete);
                return chapter;
            }
        });
    }

    private boolean checkComplete(Chapter chapter, int personalProgress) {
        Assert.notNull(chapter, "chapter不能为空");
        //章节进度小于个人当前进度，则当前章节完成
        if(chapter.getStartDay()<=personalProgress){
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
