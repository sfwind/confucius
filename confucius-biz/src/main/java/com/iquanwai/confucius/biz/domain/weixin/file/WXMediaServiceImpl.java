package com.iquanwai.confucius.biz.domain.weixin.file;

import com.iquanwai.confucius.biz.dao.wx.WXMediaDao;
import com.iquanwai.confucius.biz.po.WXMedia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 微信素材service
 */
@Service
public class WXMediaServiceImpl implements WXMediaService{
    @Autowired
    private WXMediaDao wxMediaDao;

    @Override
    public Integer insertMediaId(String mediaId, String url, String remark) {
        WXMedia wxMedia = new WXMedia();
        wxMedia.setMediaId(mediaId);
        wxMedia.setUrl(url);
        wxMedia.setRemark(remark);

        return wxMediaDao.insert(wxMedia);
    }
}
