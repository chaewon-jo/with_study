package com.project.with_study.domain.posting.repository;

import com.project.with_study.domain.posting.dto.request.SearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchRepository<T, C extends SearchCondition> {
    Page<T> search(C condition, Pageable pageable);
}
