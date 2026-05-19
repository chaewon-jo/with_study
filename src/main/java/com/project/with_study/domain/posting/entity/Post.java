package com.project.with_study.domain.posting.entity;

import com.project.with_study.domain.member.entity.Member;
import com.project.with_study.domain.posting.PostStatus;
import com.project.with_study.global.entity.BaseAuthEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE reply SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
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
    @NotEmpty
    private String title;

    @Comment("내용")
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotEmpty
    private String content;

    @Enumerated(EnumType.STRING)
    @NotEmpty
    private PostStatus status = PostStatus.OPEN;

}
