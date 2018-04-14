package com.iquanwai.confucius.web.payment.dto;

import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
import lombok.Data;

@Data
public class ApplyMappingDto {
    MemberType apply;
    MemberType wannaGoods;
}
