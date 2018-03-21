package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.dao.common.RichTextDao;
import com.iquanwai.confucius.biz.po.RichText;
import com.iquanwai.confucius.biz.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by 三十文
 */
@Service
public class FlowDataServiceImpl implements FlowDataService {

    @Autowired
    private RichTextDao richTextDao;

    @Override
    public RichText insertRichText(String title, String content) {
        RichText richText = new RichText();
        richText.setTitle(title);
        richText.setContent(content);
        richText.setUuid(CommonUtils.randomString(8));
        if (richTextDao.insertRichText(richText) > 0) {
            return richText;
        } else {
            return null;
        }
    }

}
