package com.iquanwai.confucius.biz.domain.backend.problem;

import com.iquanwai.confucius.biz.dao.fragmentation.problem.ProblemPreviewDao;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemPreview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PreviewServiceImpl implements PreviewService {
    private static final Integer INSERT_CODE = 2;
    private static final Integer UPDATE_CODE = 1;

    @Autowired
    private ProblemPreviewDao problemPreviewDao;


    @Override
    public ProblemPreview loadByProblemScheduleId(Integer problemScheduleId) {
        return problemPreviewDao.loadByScheduleId(problemScheduleId);
    }

    @Override
    public Integer updatePreview(ProblemPreview problemPreview) {
        ProblemPreview existPreview = problemPreviewDao.loadByScheduleId(problemPreview.getProblemScheduleId());
        if (existPreview == null) {
            //插入
            problemPreview.setUpdated(INSERT_CODE);
            return problemPreviewDao.insert(problemPreview);
        } else {
            //更新
            //如果是之前插入的，则还是插入的Code
            if (existPreview.getUpdated().equals(INSERT_CODE)) {
                problemPreview.setUpdated(INSERT_CODE);
            } else {
                problemPreview.setUpdated(UPDATE_CODE);
            }
            return problemPreviewDao.update(problemPreview);
        }
    }
}
