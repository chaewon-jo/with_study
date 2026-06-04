package com.project.with_study.domain.posting.entity;

import com.project.with_study.domain.member.entity.Member;
import com.project.with_study.domain.posting.PostStatus;
import com.project.with_study.global.entity.BaseAuthEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE post SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Post extends BaseAuthEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("작성자")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @NotNull
    private Member member;

    @Comment("제목")
    @Column(nullable = false)
    @Size(max = 50, message = "제목은 50자를 초과할 수 없습니다.")
    @NotEmpty(message = "제목은 필수입니다.")
    private String title;

    @Comment("내용")
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotEmpty(message = "내용은 필수입니다.")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    @Builder.Default
    private PostStatus status = PostStatus.OPEN;

    @Comment("조회수")
    @Column(nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

}
