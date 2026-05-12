package com.project.with_study.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

/**
 * 페이징 응답 메타데이터
 * page: 1부터 시작
 */
@Getter
@AllArgsConstructor
public class PageInfo {
    private int page;
    private int size;
    private long totalSize;
    private int totalPage;

    public static PageInfo from(Page<?> page) {
        return new PageInfo(
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
