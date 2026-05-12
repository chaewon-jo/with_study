package com.project.with_study.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode{
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "올바르지 않은 입력값입니다.", "제약 조건을 확인해주세요."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다.", "API 명세의 메서드를 확인해주세요."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "존재하지 않는 리소스입니다.", "요청하신 식별자의 데이터가 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 오류가 발생했습니다.", "서버 관리자에게 문의하세요."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C005", "잘못된 데이터 타입입니다.", "필드의 데이터 타입을 확인해주세요."),

    // Security
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증되지 않은 사용자입니다.", "로그인 후 다시 시도해주세요."),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "A002", "권한이 없습니다.", "해당 리소스에 접근할 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    private final String description;
}
