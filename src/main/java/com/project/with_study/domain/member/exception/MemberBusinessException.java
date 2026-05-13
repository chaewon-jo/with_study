package com.project.with_study.domain.member.exception;

import com.project.with_study.global.exception.BusinessException;
import com.project.with_study.global.exception.errorcode.ErrorCode;

public class MemberBusinessException extends BusinessException {
    public MemberBusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public MemberBusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
