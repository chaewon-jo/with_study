package com.project.with_study.domain.member.exception.errorcode;

import com.project.with_study.global.exception.errorcode.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "M001", "이미 존재하는 이메일입니다.", "다른 이메일을 입력해주세요."),
    PASSWORD_MISSMATCH(HttpStatus.BAD_REQUEST, "M002", "비밀번호가 불일치합니다.", "확인 비밀번호를 다시 확인해주세요."),
    LOGIN_MISMATCH(HttpStatus.BAD_REQUEST, "M003", "아이디 혹은 비밀번호가 틀렸습니다.", "아이디 혹은 비밀번호를 재입력해주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    private final String description;
}
