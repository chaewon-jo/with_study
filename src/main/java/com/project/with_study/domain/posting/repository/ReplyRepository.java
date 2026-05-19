package com.project.with_study.domain.posting.repository;

import com.project.with_study.domain.posting.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
}
