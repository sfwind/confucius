package com.iquanwai.confucius.biz.po;

import com.iquanwai.confucius.biz.util.page.Page;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/9/29.
 */
@Data
public class TableDto<T> {
    private List<T> data;
    private Page page;
}
