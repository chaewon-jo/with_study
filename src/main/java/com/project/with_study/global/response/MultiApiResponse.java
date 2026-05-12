package com.project.with_study.global.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class MultiApiResponse<T> extends BaseApiResponse {
    private List<T> data;
    private PageInfo pageInfo;

    public MultiApiResponse(List<T> data, Page<T> page, String message) {
        super(message);
        this.data = data;
        this.pageInfo = PageInfo.from(page);
    }

    public static <T> MultiApiResponse<T> of(List<T> data, Page<T> page) {
        return new MultiApiResponse<>(data, page, "요청이 성공적으로 처리되었습니다.");
    }
}
