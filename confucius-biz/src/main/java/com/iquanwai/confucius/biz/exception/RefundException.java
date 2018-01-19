package com.iquanwai.confucius.biz.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundException extends RuntimeException {
    private String message;
}
