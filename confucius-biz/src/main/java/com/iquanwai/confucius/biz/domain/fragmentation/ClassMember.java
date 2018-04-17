package com.iquanwai.confucius.biz.domain.fragmentation;

import lombok.Data;

/**
 * Created by 三十文
 */
@Data
public class ClassMember {
    private Integer id;
    private Integer profileId;
    private String className;
    private String groupId;
    private Integer memberTypeId;
    private Boolean active;
    private Boolean del;

}
