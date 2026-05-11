package com.project.with_study.global.exception.errorcode;

import org.springframework.http.HttpStatus;


public interface ErrorCode {
    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();
    String getDescription();
}
