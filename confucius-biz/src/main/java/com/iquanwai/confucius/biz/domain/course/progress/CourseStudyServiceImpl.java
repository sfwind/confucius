package com.iquanwai.confucius.biz.domain.course.progress;

import com.iquanwai.confucius.biz.dao.ChapterDao;
import com.iquanwai.confucius.biz.dao.CurrentChapterPageDao;
import com.iquanwai.confucius.biz.dao.MaterialDao;
import com.iquanwai.confucius.biz.dao.PageDao;
import com.iquanwai.confucius.biz.dao.po.Chapter;
import com.iquanwai.confucius.biz.dao.po.Material;
import com.iquanwai.confucius.biz.dao.po.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


    public Page loadPage(String openId, int chapterId, Integer pageSequence) {
        if(pageSequence==null){
            pageSequence = currentChapterPageDao.currentPage(openId, chapterId);
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
            currentChapterPageDao.updatePage(openId, chapterId, pageSequence);
        }
        return page;
    }

    public Chapter loadChapter(int chapterId) {
        return chapterDao.load(Chapter.class, chapterId);
    }
}
