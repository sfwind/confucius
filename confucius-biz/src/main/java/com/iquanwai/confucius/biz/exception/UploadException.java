package com.iquanwai.confucius.biz.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UploadException extends Exception {
    private String errMsg;
}
