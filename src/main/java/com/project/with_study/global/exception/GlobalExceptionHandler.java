package com.project.with_study.global.exception;

import com.project.with_study.global.exception.errorcode.ErrorCode;
import com.project.with_study.global.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import static com.project.with_study.global.exception.errorcode.CommonErrorCode.*;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * [C001] Bean Validation(@Valid 바인딩 에러)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleIllegalNotValidArgumentException(MethodArgumentNotValidException e) {
        log.warn("handle MethodArgumentNotValidException", e);

        ErrorCode errorCode = INVALID_INPUT_VALUE;
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                errorCode.getMessage()
        );

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * [C001] Bean Validation(엔티티, 파라미터 제약 조건 위반)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("handle ConstraintViolationException {}", e.getMessage());

        ErrorCode errorCode = INVALID_INPUT_VALUE;
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                errorCode.getMessage()
        );

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * [C001] 서비스 레이어 검증 위반, 잘못된 인자
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e){
        log.warn("handle IllegalArgumentException {}", e.getMessage());

        ErrorCode errorCode = INVALID_INPUT_VALUE;
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                e.getMessage()
        );

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * [C005] JSON 파싱 에러
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("handle HttpMessageNotReadableException", e);

        ErrorCode errorCode = INVALID_TYPE_VALUE;
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                "JSON 형식이 올바르지 않습니다."
        );

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * [C005] PathVariable/쿼리 파라미터 타입 에러
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("handle MethodArgumentTypeMismatchException {}, Field Name: {}", e.getMessage(), e.getName());

        ErrorCode errorCode = INVALID_TYPE_VALUE;
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                errorCode.getMessage()
        );

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * [C002] 지원하지 않는 HTTP Method 호출
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("handle HttpRequestMethodNotSupportedException {}, Method: {}", e.getMessage(), e.getMethod());

        ErrorCode errorCode = METHOD_NOT_ALLOWED;
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                errorCode.getMessage()
        );

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * [C003] 잘못된 URL 호출, 핸들러 미발견
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("handle NoHandlerFoundException {}, URL: {}", e.getMessage(), e.getRequestURL());

        ErrorCode errorCode = ENTITY_NOT_FOUND;
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                "존재하지 않는 API URL입니다."
        );

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * [A002] 접근 권한 에러(403)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("handle AccessDeniedException {}", e.getMessage());

        ErrorCode errorCode = ACCESS_DENIED;
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                errorCode.getMessage()
        );

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * [A001] 인증 실패 에러 (401)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        log.warn("handle AuthenticationException {}", e.getMessage());

        ErrorCode errorCode = UNAUTHORIZED;
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                errorCode.getMessage()
        );

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * 비즈니스 로직 예외
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("handle BusinessException [{}]: {}", e.getErrorCode(), e.getMessage());

        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                e.getMessage()
        );

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * [C001] DB 제약 조건 위반
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("handle DataIntegrityViolationException", e);

        ErrorCode errorCode = INVALID_INPUT_VALUE;
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                e.getMessage()
        );

        return new ResponseEntity<>(response, errorCode.getHttpStatus());

    }

    /**
     * [C004] 기타 모든 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("handle Exception", e);

        ErrorCode errorCode = INTERNAL_SERVER_ERROR;
        ErrorResponse response = ErrorResponse.of(errorCode);

        return new ResponseEntity<>(response, errorCode.getHttpStatus());

    }

}
