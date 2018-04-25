package com.iquanwai.confucius.biz.domain.backend.problem;

import com.iquanwai.confucius.biz.po.fragmentation.ProblemPreview;

/**
 * 课前思考service
 */
public interface PreviewService {

    /**
     * 获得课前思考
     * @param problemScheduleId
     * @return
     */
    ProblemPreview loadByProblemScheduleId(Integer problemScheduleId);



    /**
     * 添加或更新课前思考
     * @param problemPreview
     * @return
     */
    Integer updatePreview(ProblemPreview problemPreview);
}
