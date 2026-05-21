package com.project.with_study.global.response;

import com.project.with_study.global.exception.errorcode.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ErrorResponse {

    private final boolean success = false;
    private HttpStatus status;
    private String code;
    private String message;
    private List<FieldError> errors;
    private LocalDateTime timestamp;

    private ErrorResponse(ErrorCode errorCode) {
        this.status = errorCode.getHttpStatus();
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.timestamp = LocalDateTime.now();
        this.errors = new ArrayList<>();
    }

    private ErrorResponse(ErrorCode errorCode, List<FieldError> errors) {
        this.status = errorCode.getHttpStatus();
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }

    private ErrorResponse(ErrorCode errorCode, String message) {
        this.status = errorCode.getHttpStatus();
        this.code = errorCode.getCode();
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = new ArrayList<>();
    }

    private ErrorResponse(ErrorCode errorCode, List<FieldError> errors, String message) {
        this.status = errorCode.getHttpStatus();
        this.code = errorCode.getCode();
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode);
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> errors) {
        return new ErrorResponse(errorCode, errors);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode, message);
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> errors, String message) {
        return new ErrorResponse(errorCode, errors, message);
    }

}
