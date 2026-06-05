package com.project.with_study.domain.posting.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SearchCriteria {
    TITLE_AND_CONTENT("제목+내용"),
    TITLE("제목만"),
    CONTENT("내용만"),
    NICKNAME("작성자");

    private final String description;
}
