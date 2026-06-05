package com.project.with_study.domain.posting.dto.request;

import com.project.with_study.domain.member.entity.Member;
import com.project.with_study.domain.posting.PostStatus;
import com.project.with_study.domain.posting.entity.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PostCreateRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 50, message = "제목은 50자를 초과할 수 없습니다.")
        String title,
        @NotBlank(message = "내용은 필수입니다.")
        String content,
        @NotNull
        PostStatus status
) {
    public Post toPost(Member member) {
        return Post.builder()
                .title(title)
                .content(content)
                .status(status)
                .member(member)
                .build();
    }
}
