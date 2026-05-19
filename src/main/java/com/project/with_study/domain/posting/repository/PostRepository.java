package com.project.with_study.domain.posting.repository;

import com.project.with_study.domain.posting.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
