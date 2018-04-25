package com.iquanwai.confucius.biz.domain.backend.problem;

import com.iquanwai.confucius.biz.po.fragmentation.ProblemPreview;

/**
 * 课前思考service
 */
public interface PreviewService {
    /**
     * 添加或更新课前思考
     * @param problemPreview
     * @return
     */
    Integer updatePreview(ProblemPreview problemPreview);
}
