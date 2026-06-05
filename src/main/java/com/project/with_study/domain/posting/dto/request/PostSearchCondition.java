package com.project.with_study.domain.posting.dto.request;

import com.project.with_study.domain.posting.PostStatus;
import com.project.with_study.domain.posting.dto.SearchCriteria;

public record PostSearchCondition(
        SearchCriteria criteria,
        String keyword,
        PostStatus status
) implements SearchCondition {
}
