package com.project.with_study.domain.posting;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PostStatus {
    OPEN("모집 중"),
    CLOSE("모집 종료");

    private final String description;
}
