package com.project.with_study.domain.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Authority {
    MEMBER("일반 회원"),
    ADMIN("관리자");

    private final String description;
}
