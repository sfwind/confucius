package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationTypeDto {
    /**
     * 种类id
     */
    private Integer typeId;
    /**
     * 种类名
     */
    private String typeName;

}
