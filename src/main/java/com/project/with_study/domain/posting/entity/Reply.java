package com.project.with_study.domain.posting.entity;

import com.project.with_study.domain.member.entity.Member;
import com.project.with_study.global.entity.BaseAuthEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE reply SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Reply extends BaseAuthEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("댓글 작성자")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @NotNull
    private Member member;

    @Comment("댓글이 달린 모집글")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @NotNull
    private Post post;

    @Comment("댓글 내용")
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotEmpty
    private String content;

    @Comment("대댓글 작성 시 부모 댓글")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Reply parent;

    @Comment("자식 댓글들")
    @OneToMany(mappedBy = "parent")
    private List<Reply> children = new ArrayList<>();

}
