package com.iquanwai.confucius.web.pc.backend.dto;

import com.iquanwai.confucius.biz.po.fragmentation.ProblemCatalog;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSubCatalog;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 2017/9/22.
 */
@Data
public class CatalogDto {
    private List<ProblemCatalog> catalogs;
    private List<ProblemSubCatalog> subCatalogs;
}
