package com.iquanwai.confucius.web.payment.dto;

import com.iquanwai.confucius.biz.po.apply.BusinessApplySubmitVO;
import lombok.Data;

import java.util.List;

/**
 * @author nethunder
 * @version 2017-11-23
 * <p>
 * 商学院申请
 */
@Data
public class ApplySubmitDto {
    private List<BusinessApplySubmitVO> userSubmits;
    private Integer goodsId;
}
