package com.project.with_study.domain.posting.dto.response;

import com.project.with_study.domain.posting.PostStatus;
import com.project.with_study.domain.posting.entity.Post;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PostResponse(
        Long id,
        String title,
        String content,
        PostStatus status,
        String nickname,
        LocalDateTime createdAt
) {
    public static PostResponse of(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .status(post.getStatus())
                .nickname(post.getMember().getNickname())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
