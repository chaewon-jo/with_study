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
     * [C001] Bean Validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleIllegalNotValidArgumentException(MethodArgumentNotValidException e) {
        log.error("handle MethodArgumentNotValidException", e);

        ErrorCode errorCode = INVALID_INPUT_VALUE;
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                e.getMessage()
        );

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * [C001] 엔티티, 파라미터 제약 조건 위반
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("handle ConstraintViolationException", e);

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
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("handle HttpMessageNotReadableException", e);

        ErrorCode errorCode = INVALID_TYPE_VALUE;
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                e.getMessage()
        );

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * [C005] PathVariable/쿼리 파라미터 타입 에러
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("handle MethodArgumentTypeMismatchException", e);

        ErrorCode errorCode = INVALID_TYPE_VALUE;
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                e.getMessage()
        );

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * [C002] 지원하지 않는 HTTP Method 호출
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("handle HttpRequestMethodNotSupportedException", e);

        ErrorCode errorCode = METHOD_NOT_ALLOWED;
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                e.getMessage()
        );

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * [C003] 잘못된 URL 호출, 핸들러 미발견
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.error("handle NoHandlerFoundException", e);

        ErrorCode errorCode = ENTITY_NOT_FOUND;
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                e.getMessage()
        );

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * [A002] 접근 권한 에러(403)
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        log.error("handle AccessDeniedException", e);

        ErrorCode errorCode = HANDLE_ACCESS_DENIED;
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                e.getMessage()
        );

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * [A001] 인증 실패 에러 (401)
     */
    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        log.error("handle AuthenticationException", e);

        ErrorCode errorCode = UNAUTHORIZED;
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                e.getMessage()
        );

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * 비즈니스 로직 예외
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("handle BusinessException", e);

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
    protected ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
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
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("handle Exception", e);

        ErrorCode errorCode = INTERNAL_SERVER_ERROR;
        ErrorResponse response = ErrorResponse.of(errorCode);

        return new ResponseEntity<>(response, errorCode.getHttpStatus());

    }

}
